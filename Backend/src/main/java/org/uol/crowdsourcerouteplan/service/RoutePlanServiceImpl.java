package org.uol.crowdsourcerouteplan.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.transaction.Transactional;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.uol.crowdsourcerouteplan.dto.*;
import org.uol.crowdsourcerouteplan.model.*;
import org.uol.crowdsourcerouteplan.repository.*;
import org.uol.crowdsourcerouteplan.security.JWTService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class RoutePlanServiceImpl implements RoutePlanService, UserDetailsService {


    private final UserDetailsRepo userDetailsRepo;
    @Value("${ors.api.key}")
    private String orsApiKey;

    private static final int EARTH_RADIUS_KM = 6371;

    private static final int COORD_SCALE = 6;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final UserRepo userRepo;

    private final LocationRepo locationRepo;

    private final EdgeRepo edgeRepo;

    private final RoutesRepo routesRepo;

    private final ContributeRouteRepo contributeRouteRepo;

    private final RouteFeedbackRepo routeFeedbackRepo;

    private final ExternalRouteRepo externalRouteRepo;

    private final UserSavedRouteRepo userSavedRouteRepo;

    private final AuthenticationManager authManager;

    private final JWTService jwtService;

    private static final String UPLOAD_DIR = "uploads/profilePictures/";

    private final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";

    private final RouteRepository routeRepo;

    private final ExternalViaRouteRepo externalViaRouteRepo;

    private final UserActivityLogRepo userActivityLogRepo;

    public RoutePlanServiceImpl(UserRepo userRepo, LocationRepo locationRepo, EdgeRepo edgeRepo, RoutesRepo routesRepo, ContributeRouteRepo contributeRouteRepo, @Lazy AuthenticationManager authManager, JWTService jwtService, RouteRepository routeRepo, UserDetailsRepo userDetailsRepo, RouteFeedbackRepo routeFeedbackRepo, ExternalRouteRepo externalRouteRepo, UserSavedRouteRepo userSavedRouteRepo, ExternalViaRouteRepo externalViaRouteRepo, UserActivityLogRepo userActivityLogRepo) {
        this.userRepo = userRepo;
        this.locationRepo = locationRepo;
        this.edgeRepo = edgeRepo;
        this.routesRepo = routesRepo;
        this.contributeRouteRepo = contributeRouteRepo;
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.routeRepo = routeRepo;
        this.userDetailsRepo = userDetailsRepo;
        this.routeFeedbackRepo = routeFeedbackRepo;
        this.externalRouteRepo = externalRouteRepo;
        this.userSavedRouteRepo = userSavedRouteRepo;
        this.externalViaRouteRepo = externalViaRouteRepo;
        this.userActivityLogRepo = userActivityLogRepo;
    }


    @Override
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    @Override
    public void registerUser(registerdto registerdto) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if(!registerdto.getPassword().equals(registerdto.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }
        if (userRepo.existsByUsername(registerdto.getUname())) {
            throw new RuntimeException("Username already taken");
        }
        User user = new User();
        user.setUsername(registerdto.getUname());
        user.setPassword(passwordEncoder.encode(registerdto.getPassword()));
        user.setLastLogin(LocalDateTime.now());
        user.setStatus("Active");
        user.setFullName(registerdto.getFullName());
        userRepo.save(user);
    }

    public LoginResponseDTO verify(registerdto user) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUname(), user.getPassword())
        );

        if (authentication.isAuthenticated()) {
            User dbUser = userRepo.findByUsername(user.getUname())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            dbUser.setLastLogin(LocalDateTime.now());
            userRepo.save(dbUser);

            String token = jwtService.generateToken(user.getUname());

            return new LoginResponseDTO(token, dbUser.getUsername(), dbUser.getId());
        } else {
            return null;
        }
    }

    @Override
    public RouteFeedback submitFeedback(FeedbackDTO feedbackDTO) {
        int routeId = feedbackDTO.getRouteId();
        int userId = feedbackDTO.getUserId();

        if(routeFeedbackRepo.findFirstByRouteIdAndUserId(feedbackDTO.getRouteId(), feedbackDTO.getUserId()).isPresent()){
            throw new IllegalStateException("You have already given rating and comment for this route.");
        }

        int rating = feedbackDTO.getRating();
        if(rating < 1 || rating > 5){
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        String routeType = feedbackDTO.getRouteType();
        if ("INTERNAL".equalsIgnoreCase(routeType)) {
            contributeRouteRepo.findById(routeId)
                    .orElseThrow(() -> new IllegalArgumentException("Internal route not found with ID: " + routeId));
        } else if ("EXTERNAL".equalsIgnoreCase(routeType)) {
            externalRouteRepo.findById(routeId)
                    .orElseThrow(() -> new IllegalArgumentException("External route not found with ID: " + routeId));
        } else {
            throw new IllegalArgumentException("Invalid route type. Must be INTERNAL or EXTERNAL.");
        }
        RouteFeedback feedback = new RouteFeedback();
        feedback.setRouteId(routeId);
        feedback.setUserId(userId);
        feedback.setRating(rating);
        feedback.setComment(feedbackDTO.getComment());
        feedback.setCreatedAt(LocalDateTime.now());
        feedback.setRouteType(routeType.toUpperCase());
        routeFeedbackRepo.save(feedback);

        return null;
    }

    @Override
    public List<feedbackshowdto> getFeedbacks(String routeType, int routeId) {

        List<RouteFeedback> feedbacks = routeFeedbackRepo.findByRouteIdAndRouteType(routeId, routeType);

        return feedbacks.stream()
                .map(f -> {
                    // Fetching user's full name using userId
                    String fullName = userRepo.findById(f.getUserId())
                            .map(user -> user.getFullName())
                            .orElse("Anonymous");

                    return new feedbackshowdto(fullName, f.getComment(), f.getRating());
                })
                .toList();
    }

//    @Override
//    public Integer findRouteIdByCoordinates(Double startLat, Double startLon, Double endLat, Double endLon) {
//        Optional<ExternalRoute> route = externalRouteRepo.findByStartLatAndStartLonAndEndLatAndEndLon(
//                startLat, startLon, endLat, endLon);
//
//        return route.map(ExternalRoute::getId).orElse(null);
//    }

    @Override
    public CoordinatesDTO getCoordinatesForLoc(String place) {
        try {
            String url = NOMINATIM_URL + "?q=" + URLEncoder.encode(place, StandardCharsets.UTF_8)
                    + "&format=json&limit=1";

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "YourAppNameHere");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = reader.lines().collect(Collectors.joining());
            reader.close();

            JSONArray jsonArray = new JSONArray(response);

            if (jsonArray.isEmpty()) {
                throw new RuntimeException("Place not found.");
            }

            JSONObject place1 = jsonArray.getJSONObject(0);

            double lat = Double.parseDouble(place1.getString("lat"));
            double lon = Double.parseDouble(place1.getString("lon"));

            CoordinatesDTO location = new CoordinatesDTO();
            location.setLatitude(lat);
            location.setLongitude(lon);


            return location;

        } catch (Exception e) {
            throw new RuntimeException("Error fetching location: " + e.getMessage(), e);
        }
    }

//    @Override
//    public Integer getOrCreateExternalRoute(String startLocation, String endLocation) {
//        try {
//            // Step 1: Get coordinates for start and end locations
//            CoordinatesDTO startCoords = getCoordinatesForLoc(startLocation);
//            CoordinatesDTO endCoords = getCoordinatesForLoc(endLocation);
//
//            // Step 2: Check if route exists
//            Optional<ExternalRoute> routeOptional = externalRouteRepo.findByStartLatAndStartLonAndEndLatAndEndLon(
//                    startCoords.getLatitude(), startCoords.getLongitude(),
//                    endCoords.getLatitude(), endCoords.getLongitude()
//            );
//
//            if (routeOptional.isPresent()) {
//                return routeOptional.get().getId();
//            }
//
//            // Step 3: Save new external route
//            ExternalRoute newRoute = new ExternalRoute();
//            newRoute.setStartLat(startCoords.getLatitude());
//            newRoute.setStartLon(startCoords.getLongitude());
//            newRoute.setEndLat(endCoords.getLatitude());
//            newRoute.setEndLon(endCoords.getLongitude());
//
//            ExternalRoute saved = externalRouteRepo.save(newRoute);
//            return saved.getId();
//
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to get or create external route: " + e.getMessage());
//        }
//    }

    @Override
    public Integer findOrCreateRouteIdByLocationNames(
            String startLocation, String endLocation, String mode, String routeType) throws IOException, ParseException {

        // 1) Resolving to coordinates (accept anything)
        double[] s = geocodeLocation(startLocation);
        double[] e = geocodeLocation(endLocation);

        // 2) Normalizing once n use everywhere
        String startNorm = normCoordStr(s[0], s[1]);
        String endNorm   = normCoordStr(e[0], e[1]);

        if ("INTERNAL".equalsIgnoreCase(routeType)) {
            Integer id = findInternalIdFuzzy(startLocation, endLocation, mode);
            return id; // returns null if not found;
        }

        // EXTERNAL flow
        Optional<ExternalRoute> found = externalRouteRepo
                .findByStartCoordinatesAndEndCoordinatesAndMode(startNorm, endNorm, mode);

        if (found.isPresent()) return found.get().getId();

        // if Not found then fetch & save external using the SAME normalized strings
        JsonNode routeJson = fetchRouteReal(startLocation, endLocation, mode);
        saveExternalRouteToDb(s, e, routeJson.get("geometry").asText(), mode); // see below


        return externalRouteRepo
                .findByStartCoordinatesAndEndCoordinatesAndMode(startNorm, endNorm, mode)
                .map(ExternalRoute::getId)
                .orElse(null);
    }


    public Integer findInternalIdFuzzy(String startLocation, String endLocation, String mode) throws IOException, ParseException {
        // 1) input to coords (address or "lat,lon")
        double[] s = geocodeLocation(startLocation);
        double[] e = geocodeLocation(endLocation);

        // 2) Fetching candidates by mode+INTERNAL
        List<ContributeRoute> candidates = contributeRouteRepo.findByModeAndRouteType(mode, "INTERNAL");
        if (candidates == null || candidates.isEmpty()) return null;

        // 3) Selecting Closest by sum of start+end distances (meters)
        final double THRESH = 150.0; // allow ~150 m tolerance (tune as you like)

        ContributeRoute best = null;
        double bestScore = Double.MAX_VALUE;

        for (ContributeRoute c : candidates) {
            double[] sDb = parseStoredCoord(c.getStartCoordinates());
            double[] eDb = parseStoredCoord(c.getEndCoordinates());

            // forward
            double dFwd = haversineMeters(s[0], s[1], sDb[0], sDb[1]) +
                    haversineMeters(e[0], e[1], eDb[0], eDb[1]);

            // reverse
            double dRev = haversineMeters(s[0], s[1], eDb[0], eDb[1]) +
                    haversineMeters(e[0], e[1], sDb[0], sDb[1]);

            double d = Math.min(dFwd, dRev);
            if (d < bestScore) {
                bestScore = d;
                best = c;
            }
        }

        // 4) Accepts if within threshold
        if (best != null && bestScore <= 2 * THRESH) {
            return best.getId();
        }
        return null;
    }


    // Parses lat, lon or lat,lon variants safely
    private double[] parseStoredCoord(String s) {
        String t = s.trim().replaceAll("[\\[\\]]", "");
        String[] parts = t.split("\\s*,\\s*");
        if (parts.length != 2) throw new IllegalArgumentException("Bad coord: " + s);
        return new double[]{ Double.parseDouble(parts[0]), Double.parseDouble(parts[1]) };
    }





    private String normCoordStr(double lat, double lon) {
        BigDecimal la = BigDecimal.valueOf(lat).setScale(COORD_SCALE, RoundingMode.HALF_UP);
        BigDecimal lo = BigDecimal.valueOf(lon).setScale(COORD_SCALE, RoundingMode.HALF_UP);
        // EXACT same format everywhere:
        return "[" + la.toPlainString() + ", " + lo.toPlainString() + "]";
    }

    private double[] parseCoordinates(String input) {
        String s = input.trim()
                .replaceAll("^\\[|\\]$", "")     // strip brackets
                .replaceAll("\\s+", " ")         // compress spaces
                .replaceAll(";", ",");           // allow ; as comma
        if (s.matches("\\-?\\d+(?:\\.\\d+)?\\s*,\\s*\\-?\\d+(?:\\.\\d+)?")
                || s.matches("\\-?\\d+(?:\\.\\d+)?\\s+\\-?\\d+(?:\\.\\d+)?")) {
            String[] parts = s.contains(",") ? s.split(",") : s.split(" ");
            return new double[] {
                    Double.parseDouble(parts[0].trim()),
                    Double.parseDouble(parts[1].trim())
            };
        }
        throw new IllegalArgumentException("Not coordinate format: " + input);
    }




    @Override
    public ContributeRoute saveContributedRoute(ContributeRouteRequest request) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String coordinatesJson = mapper.writeValueAsString(request.getCoordinates());

        ContributeRoute route = new ContributeRoute();
        route.setUserId(request.getUserId());
        route.setRouteType("INTERNAL");
        route.setDescription(request.getDescription());
        route.setCoordinatesJson(coordinatesJson);
        route.setTransportMode(request.getTransportMode());

        return contributeRouteRepo.save(route);
    }

    public List<UserContributedRouteDTO> findMatchingRoutes(
            String startLat, String startLon, String endLat, String endLon, String mode) {

        List<ContributeRoute> allRoutes = contributeRouteRepo.findByTransportMode(mode);
        List<UserContributedRouteDTO> result = new ArrayList<>();

        double sLat = Double.parseDouble(startLat);
        double sLon = Double.parseDouble(startLon);
        double eLat = Double.parseDouble(endLat);
        double eLon = Double.parseDouble(endLon);

        final double TOL = 250.0;

        for (ContributeRoute route : allRoutes) {
            try {
                List<List<Double>> coords = new ObjectMapper().readValue(
                        route.getCoordinatesJson(),
                        new com.fasterxml.jackson.core.type.TypeReference<List<List<Double>>>() {}
                );
                if (coords == null || coords.isEmpty()) continue;

                List<Double> first = coords.get(0);
                List<Double> last  = coords.get(coords.size() - 1);

                // forward A->B
                boolean forward = isNear(first, sLat, sLon, TOL) && isNear(last, eLat, eLon, TOL);
                // reverse B->A (user could search in the opposite direction)
                boolean reverse = isNear(first, eLat, eLon, TOL) && isNear(last, sLat, sLon, TOL);

                if (forward || reverse) {
                    String username = userRepo.findById(route.getUserId())
                            .map(User::getUsername)
                            .orElse("Unknown");

                    result.add(new UserContributedRouteDTO(
                            coords,
                            username,
                            route.getDescription()
                    ));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    private static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000.0; // meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    private static boolean isNear(List<Double> pt, double lat, double lon, double tolMeters) {
        if (pt == null || pt.size() < 2) return false;
        double d = haversineMeters(pt.get(0), pt.get(1), lat, lon);
        return d <= tolMeters;
    }





    @Override
    public RoutePathWithSegmentsDTO findOptimizedRouteWithViaStops(
            String startLocation,
            List<String> viaStops,
            String endLocation,
            String transportMode
    ) {
        List<String> allPoints = new ArrayList<>();
        allPoints.add(startLocation);
        if (viaStops != null && !viaStops.isEmpty()) {
            allPoints.addAll(viaStops);
        }
        allPoints.add(endLocation);

        double totalDistance = 0.0;
        double totalDurationHours = 0.0;
        List<RouteSegmentsDTO> allSegments = new ArrayList<>();

        for (int i = 0; i < allPoints.size() - 1; i++) {
            String from = allPoints.get(i);
            String to = allPoints.get(i + 1);

            RoutePathWithSegmentsDTO segmentResult = findOptimizedRoute(from, to, transportMode);

            totalDistance += segmentResult.getTotalDistance();
            // Converts formatted duration back to hours for summing
            totalDurationHours += parseDurationToHours(segmentResult.getDuration());
            allSegments.addAll(segmentResult.getSegments());
        }

        return new RoutePathWithSegmentsDTO(
                Math.round(totalDistance * 100.0) / 100.0,
                formatDuration(totalDurationHours),
                allSegments
        );
    }

    @Override
    public Map<String, Object> getRouteWithViaStopsORS(RouteWithViaRequestDTO request) {
        try {
            // builds ordered stop list (names)
            List<String> allStops = new ArrayList<>();
            allStops.add(request.getStartLocation());
            if (request.getViaStops() != null && !request.getViaStops().isEmpty()) {
                allStops.addAll(request.getViaStops());
            }
            allStops.add(request.getEndLocation());

            // Geocode ONCE and normalizes to "lat,lon" strings
            List<double[]> coords = new ArrayList<>(allStops.size());
            for (String name : allStops) coords.add(geocodeLocation(name)); // [lat, lon]

            String startCoordsStr = toCoordString(coords.get(0));
            String endCoordsStr   = toCoordString(coords.get(coords.size() - 1));

            List<String> viaCoordStrings = new ArrayList<>();
            if (coords.size() > 2) {
                for (int i = 1; i < coords.size() - 1; i++) {
                    viaCoordStrings.add(toCoordString(coords.get(i)));
                }
            }
            String viaCoordsJson = objectMapper.writeValueAsString(viaCoordStrings);
            String mode = request.getTransportMode();

            //cache check
            boolean exists = externalViaRouteRepo
                    .findByStartCoordsAndEndCoordsAndViaCoordsAndMode(
                            startCoordsStr, endCoordsStr, viaCoordsJson, mode)
                    .isPresent();

            //if not present in table, inserts
            if (!exists) {
                ExternalViaRoute row = new ExternalViaRoute();
                row.setStartCoords(startCoordsStr);
                row.setEndCoords(endCoordsStr);
                row.setViaCoords(viaCoordsJson);
                row.setMode(mode);
                externalViaRouteRepo.save(row);
            }

            //existing ORS logic (reusing geocoded coords)
            List<Map<String, Object>> segments = new ArrayList<>();
            double totalDistance = 0;
            double totalDuration = 0;

            for (int i = 0; i < allStops.size() - 1; i++) {
                double[] fromCoords = coords.get(i);
                double[] toCoords   = coords.get(i + 1);

                String orsUrl = "https://api.openrouteservice.org/v2/directions/" + mode;

                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", orsApiKey);
                headers.setContentType(MediaType.APPLICATION_JSON);

                Map<String, Object> body = new HashMap<>();
                body.put("coordinates", List.of(
                        List.of(fromCoords[1], fromCoords[0]),
                        List.of(toCoords[1], toCoords[0])
                ));
                body.put("instructions", true);
                body.put("instructions_format", "text");

                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<JsonNode> response = restTemplate.exchange(
                        orsUrl, HttpMethod.POST, new HttpEntity<>(body, headers), JsonNode.class
                );

                JsonNode route = response.getBody().get("routes").get(0);

                double distance = route.get("summary").get("distance").asDouble() / 1000.0; // km
                double duration = route.get("summary").get("duration").asDouble() / 60.0;   // minutes
                String formattedDuration = String.format("%.0f hr %.0f min", duration / 60, duration % 60);

                totalDistance += distance;
                totalDuration += duration;

                Map<String, Object> seg = new HashMap<>();
                seg.put("from", Map.of(
                        "id", i + 1,
                        "name", allStops.get(i),
                        "latitude", fromCoords[0],
                        "longitude", fromCoords[1]
                ));
                seg.put("to", Map.of(
                        "id", i + 2,
                        "name", allStops.get(i + 1),
                        "latitude", toCoords[0],
                        "longitude", toCoords[1]
                ));
                seg.put("geometry", route.get("geometry").asText());
                seg.put("distance", distance);
                seg.put("duration", formattedDuration);

                // steps
                List<Map<String, Object>> stepList = new ArrayList<>();
                JsonNode seg0 = route.path("segments").isArray() && route.path("segments").size() > 0
                        ? route.path("segments").get(0) : null;

                if (seg0 != null && seg0.has("steps")) {
                    for (JsonNode st : seg0.get("steps")) {
                        Map<String, Object> stMap = new HashMap<>();
                        stMap.put("instruction", st.path("instruction").asText(""));
                        stMap.put("distance", st.path("distance").asDouble(0));
                        stMap.put("duration", st.path("duration").asDouble(0));
                        if (st.has("way_points") && st.get("way_points").isArray() && st.get("way_points").size() == 2) {
                            stMap.put("way_points", List.of(
                                    st.get("way_points").get(0).asInt(),
                                    st.get("way_points").get(1).asInt()
                            ));
                        }
                        stepList.add(stMap);
                    }
                }
                seg.put("steps", stepList);

                segments.add(seg);
            }

            String totalFormattedDuration = String.format("%.0f hr %.0f min", totalDuration / 60, totalDuration % 60);

            Map<String, Object> result = new HashMap<>();
            result.put("totalDistance", totalDistance);
            result.put("duration", totalFormattedDuration);
            result.put("segments", segments);

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Error fetching ORS route with via stops: " + e.getMessage(), e);
        }
    }

    @Override
    public Integer saveUserRoute(SavedRouteRequest request) {
        Optional<UserSavedRoute> existing =
                userSavedRouteRepo.findFirstByUserIdAndRouteIdAndRouteType(
                        request.getUserId(), request.getRouteId(), request.getRouteType());

        if (existing.isPresent()) {
            return existing.get().getId();
        }

        // Otherwise creates a new row
        UserSavedRoute e = new UserSavedRoute();
        e.setUserId(request.getUserId());
        e.setRouteId(request.getRouteId());
        e.setRouteType(request.getRouteType());
        e.setName(request.getName());
        e.setDescription(request.getDescription());

        return userSavedRouteRepo.save(e).getId();
    }

    @Override
    public Map<String,Object> savedListSummary(Integer userId,Integer savedId) throws IOException, ParseException {
        UserSavedRoute saved  = userSavedRouteRepo.findById(savedId)
                .orElseThrow(() -> new IllegalArgumentException("Saved route not found: " + savedId));

        // ownership check
        if (!saved.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Saved route does not belong to user: " + userId);
        }

        String from = null;
        String to = null;
        String mode = null;

        switch (saved.getRouteType()) {
            case "EXTERNAL" -> {
                ExternalRoute ext = externalRouteRepo.findById(saved.getRouteId())
                        .orElseThrow(() -> new IllegalArgumentException("External route not found: " + saved.getRouteId()));
                from = ext.getStartCoordinates();
                to   = ext.getEndCoordinates();
                mode = ext.getMode();

                JsonNode node = fetchRouteReal(from, to, mode);
                return objectMapper.convertValue(node, new TypeReference<Map<String,Object>>() {});
            }
            case "INTERNAL" -> {
                var intr = contributeRouteRepo.findById(saved.getRouteId())
                        .orElseThrow(() -> new IllegalArgumentException("Internal route not found: " + saved.getRouteId()));
                from = intr.getStartCoordinates();
                to   = intr.getEndCoordinates();
                mode = intr.getTransportMode();

                JsonNode node = fetchRouteReal(from, to, mode);
                return objectMapper.convertValue(node, new TypeReference<Map<String,Object>>() {});
            }

            case "EXTERNALVIA" -> {
                ExternalViaRoute ev = externalViaRouteRepo.findById(saved.getRouteId())
                        .orElseThrow(() -> new IllegalArgumentException("External VIA route not found: " + saved.getRouteId()));

                // viaCoords is JSON array of "lat,lon" strings
                List<String> viaStops = (ev.getViaCoords()==null || ev.getViaCoords().isBlank())
                        ? List.of()
                        : objectMapper.readValue(ev.getViaCoords(), new TypeReference<List<String>>() {});


                RouteWithViaRequestDTO dto = new RouteWithViaRequestDTO();
                dto.setStartLocation(ev.getStartCoords());
                dto.setViaStops(viaStops);
                dto.setEndLocation(ev.getEndCoords());
                dto.setTransportMode(ev.getMode());


                Map<String,Object> viaRes = getRouteWithViaStopsORS(dto);

                // Convert segments[] - single polyline + merged steps, add start/end strings
                return flattenViaSegmentsToSingle(viaRes);
            }

            default -> throw new IllegalArgumentException("Unsupported routeType: " + saved.getRouteType());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> flattenViaSegmentsToSingle(Map<String, Object> viaRes) {
        List<Map<String,Object>> segments = (List<Map<String,Object>>) viaRes.get("segments");
        if (segments == null || segments.isEmpty()) {
            throw new IllegalStateException("No segments returned for VIA route");
        }

        List<double[]> fullPath = new ArrayList<>();
        List<Map<String,Object>> stepsOut = new ArrayList<>();
        int offset = 0;

        for (Map<String,Object> seg : segments) {
            String encoded = String.valueOf(seg.get("geometry")); // encoded polyline for that leg
            List<double[]> decoded = PolylineCodec.decode(encoded); //[ [lat,lon] ...]
            fullPath.addAll(decoded);

            List<Map<String,Object>> segSteps = (List<Map<String,Object>>) seg.get("steps");
            if (segSteps != null) {
                for (Map<String,Object> st : segSteps) {
                    Map<String,Object> s2 = new HashMap<>(st);
                    Object wpObj = st.get("way_points");
                    if (wpObj instanceof List<?> wp && wp.size() == 2) {
                        int a = ((Number) wp.get(0)).intValue() + offset;
                        int b = ((Number) wp.get(1)).intValue() + offset;
                        s2.put("way_points", List.of(a, b));
                    }
                    stepsOut.add(s2);
                }
            }
            offset += decoded.size();
        }

        String combined = PolylineCodec.encode(fullPath);

        Map<String,Object> firstFrom = (Map<String,Object>) segments.get(0).get("from");
        Map<String,Object> lastTo    = (Map<String,Object>) segments.get(segments.size()-1).get("to");

        String startStr = "[" + firstFrom.get("latitude") + "," + firstFrom.get("longitude") + "]";
        String endStr   = "[" + lastTo.get("latitude")  + "," + lastTo.get("longitude")  + "]";

        Map<String,Object> out = new HashMap<>();
        out.put("geometry", combined);                 // single encoded polyline
        out.put("steps", stepsOut);                    // merged steps with corrected way_points
        out.put("startLocation", startStr);            // strings like "[lat,lon]" (FE does JSON.parse)
        out.put("endLocation",   endStr);

        //  FE shows duration text and distance if present
        out.put("distance", viaRes.get("totalDistance")); // km
        out.put("duration", viaRes.get("duration"));      // "X hr Y min"
        return out;
    }

    @Transactional
    public void deleteUserRoute(Integer savedId, Integer userId) {
        userSavedRouteRepo.findById(savedId).ifPresent(e -> {
            if (e.getUserId().equals(userId)) userSavedRouteRepo.delete(e);
        });
    }

    @Override
    public Object getUserRoute(Integer savedId) {
        return null;
    }

    @Override
    public List<UserSavedRoute> getAllUserSavedRoutes(Integer userId) {
        return userSavedRouteRepo.findAllByUserId(userId);
    }

    private static String toCoordString(double[] latlon) {
        // precision consistent
        return String.format(Locale.ROOT, "%.6f,%.6f", latlon[0], latlon[1]);
    }

    @Transactional
    public Integer getOrCreateEcternalViaRouteId(RouteWithViaRequestDTO request) {
        try {
            // Normalizing coordinates (lat,lon fixed precision)
            String startCoords = toCoordString(geocodeLocation(request.getStartLocation()));
            String endCoords   = toCoordString(geocodeLocation(request.getEndLocation()));

            List<String> viaCoordStrings = new ArrayList<>();
            if (request.getViaStops() != null) {
                for (String v : request.getViaStops()) {
                    viaCoordStrings.add(toCoordString(geocodeLocation(v)));
                }
            }
            String viaCoordsJson = objectMapper.writeValueAsString(viaCoordStrings);
            String mode = request.getTransportMode();

            // 1) Try cache
            Optional<ExternalViaRoute> existing =
                    externalViaRouteRepo.findByStartCoordsAndEndCoordsAndViaCoordsAndMode(
                            startCoords, endCoords, viaCoordsJson, mode);

            if (existing.isPresent()) return existing.get().getId();

            // 2) validate via ORS; you asked to store it anyway, so we can skip validation.
            ExternalViaRoute row = new ExternalViaRoute();
            row.setStartCoords(startCoords);
            row.setEndCoords(endCoords);
            row.setViaCoords(viaCoordsJson);
            row.setMode(mode);

            ExternalViaRoute saved = externalViaRouteRepo.save(row);
            return saved.getId();

        } catch (Exception e) {
            throw new RuntimeException("Failed to create/find ExternalViaRouteId: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ActivityFeedItem> getAllForUser(int userId) {
        return userActivityLogRepo.findAllForUser(userId).stream().map(row -> {
            ActivityFeedItem dto = new ActivityFeedItem();
            dto.setId(row.getId());
            dto.setUserId(row.getUserId());
            dto.setTitle(row.getTitle());
            dto.setAction(row.getAction());
            dto.setTime(row.getTime().toInstant());
            return dto;
        }).toList();
    }


    // Parse "X hr Y min" to hours
    private double parseDurationToHours(String formatted) {
        int hrs = 0, mins = 0;
        String[] parts = formatted.split(" ");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals("hr")) hrs = Integer.parseInt(parts[i - 1]);
            else if (parts[i].equals("min")) mins = Integer.parseInt(parts[i - 1]);
        }
        return hrs + (mins / 60.0);
    }

    private String formatDuration(double hours) {
        int totalMinutes = (int) Math.round(hours * 60);
        int hrs = totalMinutes / 60;
        int mins = totalMinutes % 60;
        if (hrs > 0 && mins > 0) {
            return hrs + " hr " + mins + " min";
        } else if (hrs > 0) {
            return hrs + " hr";
        } else {
            return mins + " min";
        }
    }

    @Override
    public Location fetchAndSaveLocation(String place) {
        try {
            if (locationRepo.existsByName(place)) {
                throw new RuntimeException("Location already exists.");
            }

            String url = NOMINATIM_URL + "?q=" + URLEncoder.encode(place, StandardCharsets.UTF_8)
                    + "&format=json&limit=1";

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "YourAppNameHere");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = reader.lines().collect(Collectors.joining());
            reader.close();

            JSONArray jsonArray = new JSONArray(response);

            if (jsonArray.isEmpty()) {
                throw new RuntimeException("Place not found.");
            }

            JSONObject place1 = jsonArray.getJSONObject(0);

            double lat = Double.parseDouble(place1.getString("lat"));
            double lon = Double.parseDouble(place1.getString("lon"));

            Location location = new Location();
            location.setName(place);
            location.setLatitude(lat);
            location.setLongitude(lon);
            locationRepo.save(location);

            return location;

        } catch (Exception e) {
            throw new RuntimeException("Error fetching location: " + e.getMessage(), e);
        }
    }

    @Override
    public CoordinatesDTO getLocationByName(String place) {
        Location location = locationRepo.findByNameIgnoreCase(place)
                .orElseThrow(() -> new RuntimeException("Place not found"));
        return new CoordinatesDTO(location.getLatitude(), location.getLongitude());
    }

    @Override
    public JsonNode fetchRouteReal(String source, String destination,String mode) throws IOException, ParseException {
        double[] sourceCoords = geocodeLocation(source);
        double[] destCoords = geocodeLocation(destination);

        List<String> allowedModes = List.of("driving-car","driving-hgv","cycling-regular","foot-walking");
        if(!allowedModes.contains(mode)) {
            mode = "driving-car";
        }

        String orsUrl = "https://api.openrouteservice.org/v2/directions/" + mode;

        Map<String, Object> payload = new HashMap<>();
        List<List<Double>> coordinates = new ArrayList<>();
        coordinates.add(List.of(sourceCoords[1], sourceCoords[0]));
        coordinates.add(List.of(destCoords[1], destCoords[0]));
        payload.put("coordinates", coordinates);

        String jsonPayload = objectMapper.writeValueAsString(payload);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(orsUrl);
            post.setHeader("Authorization", orsApiKey);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(jsonPayload, ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = client.execute(post)) {
                String result = EntityUtils.toString(response.getEntity());

                JsonNode root = objectMapper.readTree(result);
                JsonNode routesArray = root.path("routes");

                if (!routesArray.isArray() || routesArray.size() == 0) {
                    throw new RuntimeException("No routes found.");
                }

                JsonNode route = routesArray.get(0);
                JsonNode geometryNode = route.path("geometry");
                JsonNode summaryNode = route.path("summary");
                JsonNode segments = route.path("segments");
                JsonNode steps = segments.get(0).path("steps");

                if (geometryNode.isMissingNode() || geometryNode.isNull()) {
                    throw new RuntimeException("Geometry not found.");
                }

                JsonNode summaryNode1 = routesArray.get(0).path("summary");
                if (summaryNode1.isMissingNode() || summaryNode1.isNull()) {
                    throw new RuntimeException("Summary not found.");
                }
                double distance = summaryNode.path("distance").asDouble();
                double duration = summaryNode.path("duration").asDouble();


                ObjectNode cleanOutput = objectMapper.createObjectNode();
                cleanOutput.put("startLocation", source);
                cleanOutput.put("endLocation", destination);
                cleanOutput.put("geometry", geometryNode.asText());
                cleanOutput.put("distance", distance);
                cleanOutput.put("duration", duration);
                cleanOutput.set("steps", steps);
                double distanceMiles = distance / 1609.34;
                distanceMiles = Math.round(distanceMiles * 100) / 100.0;

                int totalMinutes = (int) Math.round(duration / 60);
                int hours = totalMinutes / 60;
                int minutes = totalMinutes % 60;

                String formattedDuration;
                if (hours > 0 && minutes > 0) {
                    formattedDuration = hours + " hr " + minutes + " minutes";
                } else if (hours > 0) {
                    formattedDuration = hours + " hr";
                } else {
                    formattedDuration = minutes + " minutes";
                }

                cleanOutput.put("distance", distanceMiles);
                cleanOutput.put("duration", formattedDuration);
                cleanOutput.put("distance", distanceMiles);
                cleanOutput.put("duration", formattedDuration);

                saveExternalRouteToDb(sourceCoords,destCoords,geometryNode.asText(),mode);
                return cleanOutput;
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Integer saveExternalRouteToDb(double[] startCoords, double[] endCoords, String polyline, String mode) {
        // 1) Normalizing coordinates to a canonical string (fixed precision + spacing)
        String startNorm = normCoordStr(startCoords[0], startCoords[1]);
        String endNorm   = normCoordStr(endCoords[0], endCoords[1]);

        // 2) finds existing (startvtoend)
        Optional<ExternalRoute> existing = externalRouteRepo
                .findByStartCoordinatesAndEndCoordinatesAndMode(startNorm, endNorm, mode);

        // 3) Optionally treats reverse (end to start) as the same route
        if (existing.isEmpty()) {
            existing = externalRouteRepo
                    .findByStartCoordinatesAndEndCoordinatesAndMode(endNorm, startNorm, mode);
        }

        if (existing.isPresent()) {
            return existing.get().getId();
        }

        // 4) Not found then creates new
        ExternalRoute route = new ExternalRoute();
        route.setStartCoordinates(startNorm);
        route.setEndCoordinates(endNorm);
        route.setPolyline(polyline);   // or setGeometry(...)
        route.setMode(mode);
        route.setCreatedAt(LocalDateTime.now());

        return externalRouteRepo.save(route).getId();
    }




    @Override
    public void generateAllDistancesFromNode(String location) {
        Location fromLocation = locationRepo.findByNameIgnoreCase(location)
                .orElseThrow(() -> new RuntimeException("Location not found."));

        List<Location> allLocations = locationRepo.findAll();

        for(Location toLocation : allLocations) {
            if (toLocation.getId().equals(fromLocation)) continue;


            int smallLocationId = Math.min(fromLocation.getId(), toLocation.getId());
            int largeLocationId = Math.max(fromLocation.getId(), toLocation.getId());
            Optional<Location> location1=locationRepo.findById(smallLocationId);
            Optional<Location> location2=locationRepo.findById(largeLocationId);
            boolean exists = edgeRepo.existsByFromLocationAndToLocation(location1, location2);

            if (exists) continue;
            double distance = haversine(fromLocation.getLatitude(), fromLocation.getLongitude(),
                    toLocation.getLatitude(), toLocation.getLongitude());

            Edge edge = new Edge();
            edge.setFromLocation(fromLocation);
            edge.setToLocation(toLocation);
            edge.setDistance(distance);

            edgeRepo.save(edge);

        }
    }

    @Override
    public List<Location> allLocations() {
        List<Location> allLocations = locationRepo.findAll();
        for(Location location : allLocations) {
            generateAllDistancesFromNode(location.getName());
        }
        return allLocations;
    }

    @Override
    public void saveRoute(RouteDTO routeDTO) {
        Route route=new Route();
        route.setFromNodeId(routeDTO.getFromNodeId());
        route.setToNodeId(routeDTO.getToNodeId());
        route.setPathJSON(new Gson().toJson(routeDTO.getRoutePath()));
        route.setDescription(routeDTO.getDescription());
        route.setRating(routeDTO.getRating());
        routesRepo.save(route);
    }



    private double haversine(double latitude, double longitude, double latitude1, double longitude1) {
        double dLat = Math.toRadians(latitude1 - latitude);
        double dLon = Math.toRadians(longitude1 - longitude);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(latitude1)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    private double[] geocodeLocation(String placeName) throws IOException, ParseException {
        String maybe = placeName.trim();
        //already coordinates
        try {
            return parseCoordinates(maybe);
        } catch (IllegalArgumentException ignore) {}

        // Global Nominatim (no country filter)
        String encoded = URLEncoder.encode(placeName, StandardCharsets.UTF_8);
        String url = "https://nominatim.openstreetmap.org/search?q=" + encoded + "&format=json&limit=1";

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(url);
            get.setHeader("User-Agent", "route-planner-app");
            try (CloseableHttpResponse response = client.execute(get)) {
                String result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                JsonNode arr = objectMapper.readTree(result);
                if (arr.isArray() && arr.size() > 0) {
                    double lat = Double.parseDouble(arr.get(0).get("lat").asText());
                    double lon = Double.parseDouble(arr.get(0).get("lon").asText());
                    return new double[]{lat, lon};
                }
            }
        }
        throw new RuntimeException("Could not geocode: " + placeName);
    }




//    public Optional<User> findUserByUsername(String username) {
//        return userRepo.findByUsername(username);
//    }


    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        org.springframework.security.core.userdetails.User.UserBuilder builder = org.springframework.security.core.userdetails.User.withUsername(user.getUsername());
        builder.password(user.getPassword());
        builder.roles("USER");
        return builder.build();
    }

    @Override
    public List<RouteResponseDTO> getAllRoutesByNames(String from, String to) {
        Location fromLoc = locationRepo.findByNameIgnoreCase(from)
                .orElseThrow(() -> new RuntimeException("From location not found"));

        Location toLoc = locationRepo.findByNameIgnoreCase(to)
                .orElseThrow(() -> new RuntimeException("To location not found"));

        List<Route> routeList = routesRepo.findAllByFromNodeIdAndToNodeId(fromLoc.getId(), toLoc.getId());

        return routeList.stream().map(route -> {
            List<Integer> path = new Gson().fromJson(route.getPathJSON(), new TypeToken<List<Integer>>(){}.getType());

            List<RouteResponseDTO.NodeDTO> nodeList = path.stream()
                    .map(id -> locationRepo.findById(id)
                            .map(loc -> new RouteResponseDTO.NodeDTO(
                                    loc.getId(), loc.getName(), loc.getLatitude(), loc.getLongitude()))
                            .orElseThrow(() -> new RuntimeException("Node not found: " + id)))
                    .toList();

            RouteResponseDTO dto = new RouteResponseDTO();
            dto.description = route.getDescription();
            dto.rating = route.getRating();
            dto.nodes = nodeList;

            return dto;
        }).toList();
    }


    @Transactional
    public void generateTop20Edges() {
        List<Location> allLocations = locationRepo.findAll();

        List<Edge> allEdges = new ArrayList<>();

        for (Location fromLoc : allLocations) {
            PriorityQueue<Edge> nearest20 = new PriorityQueue<>(Comparator.comparingDouble(Edge::getDistance));

            for (Location toLoc : allLocations) {
                if (fromLoc.getId().equals(toLoc.getId())) continue;

                double distance = haversine(fromLoc.getLatitude(), fromLoc.getLongitude(),
                        toLoc.getLatitude(), toLoc.getLongitude());

                Edge edge = new Edge();
                edge.setFromLocation(fromLoc);
                edge.setToLocation(toLoc);
                edge.setDistance(distance);

                nearest20.add(edge);
            }

            // Gets only top 20
            for (int i = 0; i < 20 && !nearest20.isEmpty(); i++) {
                allEdges.add(nearest20.poll());
            }
        }

        edgeRepo.saveAll(allEdges);
    }



    //A* Algorithm Route Engine
    @Override
    public List<Location> findShortestPath(String startName, String endName) {
        Location start = routeRepo.findLocationByName(startName);
        Location goal = routeRepo.findLocationByName(endName);

        if (start == null || goal == null) {
            throw new IllegalArgumentException("Start or goal not found.");
        }

        PriorityQueue<NodeRecord> openSet = new PriorityQueue<>(Comparator.comparingDouble(NodeRecord::getfScore));
        Map<Integer, NodeRecord> allNodes = new HashMap<>();
        Set<Integer> closedSet = new HashSet<>();

        double h = heuristic(start, goal);
        NodeRecord startRecord = new NodeRecord(start, 0.0, h, h, null);

        openSet.add(startRecord);
        allNodes.put(start.getId(), startRecord);

        while (!openSet.isEmpty()) {
            NodeRecord current = openSet.poll();

            if (current.getLocation().getId().equals(goal.getId())) {
                return reconstructPath(current);
            }

            closedSet.add(current.getLocation().getId());

            List<Edge> neighbors = routeRepo.findEdgesFromLocation(current.getLocation().getId());

            for (Edge edge : neighbors) {
                Location neighborLoc = edge.getToLocation();
                if (closedSet.contains(neighborLoc.getId())) continue;

                double tentativeG = current.getgScore() + edge.getDistance();
                NodeRecord neighborRecord = allNodes.get(neighborLoc.getId());

                if (neighborRecord == null) {
                    double hScore = heuristic(neighborLoc, goal);
                    neighborRecord = new NodeRecord(neighborLoc, tentativeG, hScore, tentativeG + hScore, current);
                    allNodes.put(neighborLoc.getId(), neighborRecord);
                    openSet.add(neighborRecord);
                } else if (tentativeG < neighborRecord.getgScore()) {
                    openSet.remove(neighborRecord);
                    neighborRecord.setgScore(tentativeG);
                    neighborRecord.setfScore(tentativeG + neighborRecord.gethScore());
                    neighborRecord.setCameFrom(current);
                    openSet.add(neighborRecord);
                }
            }
        }

        return Collections.emptyList(); // No route found
    }

//    @Override
//    public RoutePathDTO findOptimizedRoute(String startName, String endName) {
//        Location start = routeRepo.findLocationByName(startName);
//        Location goal = routeRepo.findLocationByName(endName);
//
//        if (start == null || goal == null) {
//            throw new IllegalArgumentException("Start or Goal location not found");
//        }
//
//        List<Location> path = findShortestPath(startName, endName); // A* path
//
//        if (path.isEmpty()) {
//            throw new RuntimeException("No route found between " + startName + " and " + endName);
//        }
//
//        List<LocationPoint> locationPoints = new ArrayList<>();
//        double totalDistance = 0.0;
//        Location previous = null;
//
//        for (Location current : path) {
//            double distance = 0.0;
//            if (previous != null) {
//                distance = haversineDistance(previous.getLatitude(), previous.getLongitude(),
//                        current.getLatitude(), current.getLongitude());
//                totalDistance += distance;
//
//            }
//            double totalLocDistance=Math.round(distance * 100.0) / 100.0;
//            locationPoints.add(new LocationPoint(
//                    current.getId(),
//                    current.getName(),
//                    current.getLatitude(),
//                    current.getLongitude(),
//                    totalLocDistance
//            ));
//            previous = current;
//        }
//
//        double roundedDistance = Math.round(totalDistance * 100.0) / 100.0;
//        return new RoutePathDTO(roundedDistance, locationPoints);
//    }

    public RoutePathWithSegmentsDTO findOptimizedRoute(String startName, String endName, String mode) {
        Location start = routeRepo.findLocationByName(startName);
        Location goal = routeRepo.findLocationByName(endName);

        if (start == null || goal == null) {
            throw new IllegalArgumentException("Start or Goal location not found");
        }

        List<Location> path = findShortestPath(startName, endName);
        if (path.isEmpty()) {
            throw new RuntimeException("No route found between " + startName + " and " + endName);
        }

        List<LocationPoint> locationPoints = new ArrayList<>();
        double totalDistance = 0.0;
        Location previous = null;

        for (Location current : path) {
            double distance = 0.0;
            if (previous != null) {
                distance = haversineDistance(previous.getLatitude(), previous.getLongitude(),
                        current.getLatitude(), current.getLongitude());
                totalDistance += distance;
            }
            double totalLocDistance = Math.round(distance * 100.0) / 100.0;
            locationPoints.add(new LocationPoint(
                    current.getId(),
                    current.getName(),
                    current.getLatitude(),
                    current.getLongitude(),
                    totalLocDistance
            ));
            previous = current;
        }

        ExecutorService executor = Executors.newFixedThreadPool(8);
        List<CompletableFuture<RouteSegmentsDTO>> futureSegments = new ArrayList<>();

        for (int i = 0; i < locationPoints.size() - 1; i++) {
            LocationPoint from = locationPoints.get(i);
            LocationPoint to = locationPoints.get(i + 1);

            double segmentDistance = haversineDistance(from.getLatitude(), from.getLongitude(),
                    to.getLatitude(), to.getLongitude());
            double segmentDurationHours = calculateDuration(segmentDistance, mode);

            CompletableFuture<RouteSegmentsDTO> future = CompletableFuture.supplyAsync(() -> {
                try {
                    String geometry = fetchGeometry(from.getName(), to.getName(), mode);
                    return new RouteSegmentsDTO(
                            from,
                            to,
                            geometry,
                            Math.round(segmentDistance * 100.0) / 100.0,
                            formatDuration(segmentDurationHours)
                    );
                } catch (Exception e) {
                    throw new RuntimeException("Failed to fetch geometry for segment: " +
                            from.getName() + " → " + to.getName(), e);
                }
            }, executor);

            futureSegments.add(future);
        }

        List<RouteSegmentsDTO> segments = futureSegments.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        executor.shutdown();

        double totalDurationHours = segments.stream()
                .mapToDouble(seg -> parseDurationToHours(seg.getDuration()))
                .sum();

        return new RoutePathWithSegmentsDTO(
                Math.round(totalDistance * 100.0) / 100.0,
                formatDuration(totalDurationHours),
                segments
        );
    }


    private double calculateDuration(double distanceKm, String mode) {
        double speedKmh;
        switch (mode) {
            case "driving-car":
                speedKmh = 60.0; // average speed in km/h
                break;
            case "cycling-regular":
                speedKmh = 15.0;
                break;
            case "foot-walking":
                speedKmh = 5.0;
                break;
            default:
                speedKmh = 50.0;
        }
        return distanceKm / speedKmh;
    }

    @Override
    public UserDetailsDTO getUserDetails(int userId) {
        Optional<User> user = userRepo.findById(userId);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found for userId: " + userId);
        }

        Optional<UserDetailsMgmt> details = userDetailsRepo.findByUserId(userId);

        UserDetailsDTO dto = new UserDetailsDTO();
        dto.setUserName(user.get().getUsername());
        dto.setFullName(user.get().getFullName());

        if (details.isPresent()) {
            dto.setPhoneNumber(details.get().getPhoneNumber());
            dto.setEmail(details.get().getEmail());
            dto.setBio(details.get().getBio());
            dto.setDob(details.get().getDob());
        } else {
            dto.setPhoneNumber(null);
            dto.setEmail(null);
            dto.setBio(null);
            dto.setDob(null);
        }
        return dto;
    }


    @Override
    public String saveUserDetails(UserDetailsRequestDTO userDetailsRequestDTO) {
        if (userDetailsRepo.existsByUserId(userDetailsRequestDTO.getUserId())) {
            return "User details already exist!";
        }

        UserDetailsMgmt userDetails = new UserDetailsMgmt();
        userDetails.setUserId(userDetailsRequestDTO.getUserId());
        userDetails.setPhoneNumber(userDetailsRequestDTO.getPhoneNumber());
        userDetails.setEmail(userDetailsRequestDTO.getEmail());
        userDetails.setDob(userDetailsRequestDTO.getDob());
        userDetails.setBio(userDetailsRequestDTO.getBio());// Sets from DTO if needed

        userDetailsRepo.save(userDetails);
        return "User details saved successfully.";
    }

    @Override
    public UserDetailsMgmt patchUserDetails(UserDetailsDTO updateDto) {
        Optional<User> user=userRepo.findById(updateDto.getUserId());
        UserDetailsMgmt details = userDetailsRepo.findByUserId(updateDto.getUserId())
                .orElseGet(() -> {
                    UserDetailsMgmt newDetails = new UserDetailsMgmt();
                    newDetails.setUserId(user.get().getId());
                    return newDetails;
                });


        if(updateDto.getPhoneNumber() != null) {
            details.setPhoneNumber(updateDto.getPhoneNumber());
        }
        if(updateDto.getEmail() != null) {
            details.setEmail(updateDto.getEmail());
        }
        if (updateDto.getDob() != null) {
            details.setBio(updateDto.getBio());
        }
        if (updateDto.getBio() != null){
            details.setBio(updateDto.getBio());
        }

        return userDetailsRepo.save(details);
    }

    private String fetchGeometry(String name, String name1, String mode) {
        try {
            JsonNode routeData = fetchRouteReal(name, name1, mode);
            return routeData.path("geometry").asText();
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch route geometry between " + name + " and " + name1, e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


    private List<Location> reconstructPath(NodeRecord current) {
        List<Location> path = new ArrayList<>();
        while (current != null) {
            path.add(current.getLocation());
            current = current.getCameFrom();
        }
        Collections.reverse(path);
        return path;
    }


    private double heuristic(Location neighborLoc, Location goal) {
        return haversineDistance(neighborLoc.getLatitude(), neighborLoc.getLongitude(), goal.getLatitude(), goal.getLongitude());
    }

    private double haversineDistance(double latitude, double longitude, double latitude1, double longitude1) {
        final int R = 6371; // Radius of earth in km
        double latDistance = Math.toRadians(latitude1 - latitude);
        double lonDistance = Math.toRadians(longitude1 - longitude);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(latitude1))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }


}


