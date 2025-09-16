package org.uol.crowdsourcerouteplan.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.hc.core5.http.ParseException;
import org.uol.crowdsourcerouteplan.dto.*;
import org.uol.crowdsourcerouteplan.model.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface RoutePlanService {

    List<User> getAllUsers();

    void registerUser(registerdto registerdto);


    Location fetchAndSaveLocation(String place);

    CoordinatesDTO getLocationByName(String place) throws IOException;

    JsonNode fetchRouteReal(String source, String destination,String mode) throws IOException,ParseException;

    void generateAllDistancesFromNode(String location);

    List<Location> allLocations();

    void saveRoute(RouteDTO routeDTO);

    List<RouteResponseDTO> getAllRoutesByNames(String from, String to);

    void generateTop20Edges();

    List<Location> findShortestPath(String startName, String endName);

    RoutePathWithSegmentsDTO findOptimizedRoute(String startName, String endName,String mode) throws IOException;

    UserDetailsDTO getUserDetails(int userId);

    String saveUserDetails(UserDetailsRequestDTO userDetailsRequestDTO);

    UserDetailsMgmt patchUserDetails(UserDetailsDTO updateDto);

    LoginResponseDTO verify(registerdto registerdto);

    RouteFeedback submitFeedback(FeedbackDTO feedbackDTO);

    List<feedbackshowdto> getFeedbacks(String routeType, int routeId);

    //Integer findRouteIdByCoordinates(Double startLat, Double startLon, Double endLat, Double endLon);

    CoordinatesDTO getCoordinatesForLoc(String place);

    //Integer getOrCreateExternalRoute(String startLocation, String endLocation);

    Integer findOrCreateRouteIdByLocationNames(String startLocation, String endLocation,String mode,String routeType) throws IOException, ParseException;

    ContributeRoute saveContributedRoute(ContributeRouteRequest request) throws JsonProcessingException;

    List<UserContributedRouteDTO> findMatchingRoutes(String startLat, String startLon, String endLat, String endLon,String mode) throws IOException;


    RoutePathWithSegmentsDTO findOptimizedRouteWithViaStops(String startLocation, List<String> viaStops, String endLocation, String transportMode);

    Map<String,Object> getRouteWithViaStopsORS(RouteWithViaRequestDTO request);

    Integer saveUserRoute(SavedRouteRequest request);

    Map<String, Object> savedListSummary(Integer userId,Integer savedId)throws IOException, ParseException;

    void deleteUserRoute(Integer savedId, Integer userId);

    Object getUserRoute(Integer savedId);

    List<UserSavedRoute> getAllUserSavedRoutes(Integer userId);

    Integer getOrCreateEcternalViaRouteId(RouteWithViaRequestDTO request);


    List<ActivityFeedItem> getAllForUser(int userId);
}
