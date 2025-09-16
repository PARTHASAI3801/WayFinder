import "leaflet/dist/leaflet.css";
import React, { useEffect, useState, useRef } from "react";
import { MapContainer, TileLayer, Polyline, Marker, Popup, useMap } from "react-leaflet";
import L from "leaflet";
import polyline from "@mapbox/polyline";
import "./App.css";
import { useNavigate } from "react-router-dom";
import ChatPanel from "./ChatPanel"; // keep ChatPanel.jsx in src/ next to this file

const customIcon = new L.Icon({
  iconUrl: "https://unpkg.com/leaflet@1.9.3/dist/images/marker-icon.png",
  shadowUrl: "https://unpkg.com/leaflet@1.9.3/dist/images/marker-shadow.png",
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41],
});

const userLocationIcon = new L.Icon({
  iconUrl: process.env.PUBLIC_URL + "/user.svg",
  iconSize: [40, 40],
  iconAnchor: [20, 40],
  popupAnchor: [0, -40],
  shadowUrl: null,
  shadowSize: null,
});

const colors = ["blue", "green", "red", "orange", "purple"];

function MultiRouteMap() {
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const [routes, setRoutes] = useState([]);
  const [highlightedRouteIndex, setHighlightedRouteIndex] = useState(null);
  const [expandedIndex, setExpandedIndex] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [showDrawer, setShowDrawer] = useState(false);
  const [showProfileOverlay, setShowProfileOverlay] = useState(false);

  // Saved routes overlay
  const [showSavedOverlay, setShowSavedOverlay] = useState(false);
  const [savedRoutes, setSavedRoutes] = useState([]);
  const [activeSaveId, setActiveSavedId] = useState(null);

  // History overlay
  const [showHistoryOverlay, setShowHistoryOverlay] = useState(false);
  const [historyItems, setHistoryItems] = useState([]);
  const [loadingHistory, setLoadingHistory] = useState(false);
  const [historyError, setHistoryError] = useState("");

  const [userData, setUserData] = useState({});
  const [isEditable, setIsEditable] = useState(false);
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [bio, setBio] = useState("");
  const [dob, setDob] = useState("");
  const [userLocation, setUserLocation] = useState(null);

  // feedback
  const [feedbackRating, setFeedbackRating] = useState("");
  const [feedbackComment, setFeedbackComment] = useState("");
  const [routeFeedbacks, setRouteFeedbacks] = useState([]);
  const [showFeedbackSection, setShowFeedbackSection] = useState(false);

  const [showModes, setShowModes] = useState(false);
  const [transportMode, setTransportMode] = useState("driving-car");
  const [hoverRating, setHoverRating] = useState(0);
  const [directions, setDirections] = useState([]);
  const positionWatcherRef = useRef(null);
  const [currentStepIndex, setCurrentStepIndex] = useState(null);

  const [viaStops, setViaStops] = useState([]);

  // Save modal state
  const [showSaveModal, setShowSaveModal] = useState(false);
  const [saveModalIdx, setSaveModalIdx] = useState(null);
  const [saveName, setSaveName] = useState("");
  const [saveDescription, setSaveDescription] = useState("");
  const [savingRoute, setSavingRoute] = useState(false);

  // chat state & helpers
  const LEFT_PANEL_WIDTH = 420;
  const [chatOpen, setChatOpen] = useState(false);
  function cryptoId() {
    return Math.random().toString(36).slice(2, 9);
  }
  const [chatMsgs, setChatMsgs] = useState([
    { id: cryptoId(), role: "assistant", text: "Hi! Describe your trip and I’ll plan it." },
  ]);
  const [chatInput, setChatInput] = useState("");
  const [lastPrompt, setLastPrompt] = useState("");
  const [showLocationCTA, setShowLocationCTA] = useState(false);
  // ===================================

  const navigate = useNavigate();
  

  // map ref + fly helper 
  const mapRef = useRef(null);
  const flyToRouteGeometry = (geometry) => {
  if (!mapRef.current || !Array.isArray(geometry) || geometry.length === 0) return;
  const bounds = L.latLngBounds(geometry.map(([lat, lng]) => [lat, lng]));
  if (typeof mapRef.current.flyToBounds === "function") {
    mapRef.current.flyToBounds(bounds, { padding: [60, 60], duration: 1.0, maxZoom: 16 });
  } else {
    mapRef.current.fitBounds(bounds, { padding: [60, 60], animate: true, maxZoom: 16 });
  }
};

  // ---------------------------------------------------------------

  const isViaMode = viaStops.some((s) => s && s.trim() !== "");
  const isRouteVia = (route) => (route?.description || "").startsWith("ORS Route with");

  const getDirectionIcon = (instruction) => {
    if (!instruction) return "➡️";
    const txt = String(instruction).toLowerCase();
    if (txt.includes("left") && txt.includes("slight")) return "↰";
    if (txt.includes("right") && txt.includes("slight")) return "↱";
    if (txt.includes("left") && txt.includes("sharp")) return "⤴️";
    if (txt.includes("right") && txt.includes("sharp")) return "⤵️";
    if (txt.includes("left")) return "⬅️";
    if (txt.includes("right")) return "➡️";
    if (txt.includes("continue")) return "⬆️";
    if (txt.includes("roundabout")) return "🔁";
    if (txt.includes("arrive")) return "🏁";
    return "➡️";
  };

  // time-ago + action icon helpers
  const timeAgo = (iso) => {
    const d = new Date(iso);
    const s = Math.floor((Date.now() - d.getTime()) / 1000);
    if (s < 60) return `${s}s ago`;
    const m = Math.floor(s / 60);
    if (m < 60) return `${m}m ago`;
    const h = Math.floor(m / 60);
    if (h < 24) return `${h}h ago`;
    const days = Math.floor(h / 24);
    if (days < 7) return `${days}d ago`;
    return d.toLocaleString();
  };

  const actionIcon = (action) => {
    switch (action) {
      case "SAVED_ROUTE_CREATED": return "💾";
      case "FEEDBACK_ADDED":      return "⭐";
      case "ROUTE_CONTRIBUTED":   return "🛣️";
      default:                    return "📝";
    }
  };


  const AutoZoom = ({ geometry }) => {
  const map = useMap();

  useEffect(() => {
    if (!map || !Array.isArray(geometry) || geometry.length < 2) return;
    const bounds = L.latLngBounds(geometry.map(([lat, lng]) => [lat, lng]));
    map.flyToBounds(bounds, { padding: [60, 60], duration: 1.0 });
  }, [map, geometry]);

  return null;
};


  useEffect(() => {
    const storedUserId = localStorage.getItem("userId");
    if (storedUserId) {
      fetch(`http://localhost:8080/userMgmt/getUserDetails?userId=${storedUserId}`)
        .then((res) => res.json())
        .then((data) => {
          setUserData(data);
          setFullName(data.fullName || "");
          setEmail(data.email || "");
          setPhoneNumber(data.phoneNumber || "");
          setBio(data.bio || "");
          setDob(data.dob || "");
        })
        .catch((err) => console.error("Failed to fetch user details", err));
    }
  }, []);

  useEffect(() => {
    if (!directions.length) return;

    const watchId = navigator.geolocation.watchPosition(
      (position) => {
        const userLat = position.coords.latitude;
        const userLon = position.coords.longitude;

        setUserLocation({ lat: userLat, lon: userLon });

        let closestIdx = null;
        let minDist = Infinity;

        directions.forEach((step, idx) => {
          const [startIdx, endIdx] = step.way_points || [0, 0];
          const routePoints = routes[highlightedRouteIndex]?.geometry || [];
          const midPointIdx = Math.floor((startIdx + endIdx) / 2);
          const point = routePoints[midPointIdx];
          if (!point) return;

          const dist = Math.pow(userLat - point[0], 2) + Math.pow(userLon - point[1], 2);
          if (dist < minDist) {
            minDist = dist;
            closestIdx = idx;
          }
        });

        if (closestIdx !== null) setCurrentStepIndex(closestIdx);
      },
      (err) => console.error("Error tracking location", err),
      { enableHighAccuracy: true, maximumAge: 10000, timeout: 10000 }
    );

    positionWatcherRef.current = watchId;
    return () => {
      if (positionWatcherRef.current) navigator.geolocation.clearWatch(positionWatcherRef.current);
    };
  }, [directions, highlightedRouteIndex, routes]);



useEffect(() => {
  const route =
    highlightedRouteIndex != null
      ? routes[highlightedRouteIndex]
      : routes[0];

  if (route?.geometry?.length) {
    flyToRouteGeometry(route.geometry);
  }
}, [routes, highlightedRouteIndex]);

  // via stop handlers
  const handleViaStopChange = (index, value) => {
    const updated = [...viaStops];
    updated[index] = value;
    setViaStops(updated);
  };
  const handleRemoveViaStop = (index) => {
    const updated = viaStops.filter((_, i) => i !== index);
    setViaStops(updated);
  };

  // normalize helper for "via" 
  const normalizeVia = (v) => {
    if (!v) return [];
    if (Array.isArray(v)) return v.map(String).map(s => s.trim()).filter(Boolean);
    if (typeof v === "string") return [v.trim()].filter(Boolean);
    return [];
  };

  // fetch with via stops 
  const fetchRoutesWithVia = async (modeArg, overrides = {}) => {
    const mode = modeArg ?? transportMode;

    const fromVal = (overrides.from ?? from) || "";
    const toVal   = (overrides.to   ?? to)   || "";
    const viaVals = normalizeVia(overrides.via ?? viaStops);

    setLoading(true);
    setError("");
    setCurrentStepIndex(null);
    try {
      const resORS = await fetch("http://localhost:8080/locations/getRouteWithViaStopsORS", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          startLocation: fromVal,
          viaStops: viaVals,
          endLocation: toVal,
          transportMode: mode,
        }),
      });
      if (!resORS.ok) throw new Error("Failed to fetch ORS route");
      const dataORS = await resORS.json();

      let fullGeometryORS = [];
      let allNodesORS = [];
      let allStepsORS = [];
      let offset = 0;

      (dataORS.segments || []).forEach((seg, i) => {
        const decoded = polyline.decode(seg.geometry);
        fullGeometryORS.push(...decoded);

        if (i === 0) {
          allNodesORS.push({
            id: seg.from.id,
            name: seg.from.name,
            latitude: seg.from.latitude,
            longitude: seg.from.longitude,
          });
        }
        allNodesORS.push({
          id: seg.to.id,
          name: seg.to.name,
          latitude: seg.to.latitude,
          longitude: seg.to.longitude,
        });

        const segSteps = Array.isArray(seg.steps) ? seg.steps : [];
        segSteps.forEach((st) => {
          const wp =
            Array.isArray(st.way_points) && st.way_points.length === 2
              ? [st.way_points[0] + offset, st.way_points[1] + offset]
              : undefined;

          allStepsORS.push({
            instruction: st.instruction || "",
            distance: st.distance ?? 0,
            duration: st.duration ?? 0,
            ...(wp ? { way_points: wp } : {}),
          });
        });

        offset += decoded.length;
      });

      const orsRoute = {
        geometry: fullGeometryORS,
        nodes: allNodesORS,
        description: `ORS Route with ${viaVals.length} via stop(s)`,
        distance: dataORS.totalDistance,
        duration: dataORS.duration,
        steps: allStepsORS,
      };

      setRoutes([orsRoute]);
      setHighlightedRouteIndex(0);
      setDirections(allStepsORS);

      // fly to via route
      flyToRouteGeometry(fullGeometryORS);
    } catch (err) {
      console.error(err);
      setError("Could not load route with via stops");
      setRoutes([]);
      setHighlightedRouteIndex(null);
      setDirections([]);
    } finally {
      setLoading(false);
    }
  };

  const handleModeChange = (mode) => {
    setTransportMode(mode);
    if (isViaMode) fetchRoutesWithVia(mode);
    else fetchRoutes(mode);
  };

  
  const COORD_RE = /^\s*-?\d+(\.\d+)?\s*,\s*-?\d+(\.\d+)?\s*$/;
  const toLatLng = (val) => {
    if (Array.isArray(val) && val.length >= 2) return [Number(val[0]), Number(val[1])];
    if (typeof val === "string") {
      const s = val.trim().replace(/^\[/, "").replace(/\]$/, "");
      if (COORD_RE.test(s)) {
        const [lat, lng] = s.split(",").map((n) => Number(n.trim()));
        return [lat, lng];
      }
    }
    return null;
  };


  const geocodeToPair = async (src, dst, mode) => {
    const res = await fetch(
      `http://localhost:8080/locations/getRoutePath?source=${encodeURIComponent(src)}&destination=${encodeURIComponent(dst)}&mode=${encodeURIComponent(mode)}`
    );
    if (!res.ok) throw new Error("Failed to geocode via getRoutePath");
    const data = await res.json();
    if (!data.geometry) throw new Error("No geometry from geocode");
    const coords = polyline.decode(data.geometry);
    if (!coords.length) throw new Error("Empty geometry from geocode");
    const start = coords[0];                      // [lat, lon]
    const end = coords[coords.length - 1];        // [lat, lon]
    return { start, end };
  };

  const fetchUserContributedRoutes = async (fromInput, toInput, mode) => {
    try {
      //  parse as coords
      let start = toLatLng(fromInput);
      let end = toLatLng(toInput);

      //  If they are addresses, geocode via getRoutePath
      if (!start || !end) {
        const { start: gStart, end: gEnd } = await geocodeToPair(fromInput, toInput, mode);
        if (!start) start = gStart;
        if (!end) end = gEnd;
      }

      const [startLat, startLng] = start;
      const [endLat, endLng] = end;

      //  doing Query contributed routes with clean numeric params
      const response = await fetch(
        `http://localhost:8080/locations/userContributedRoutes?startLat=${encodeURIComponent(
          startLat
        )}&startLng=${encodeURIComponent(startLng)}&endLat=${encodeURIComponent(
          endLat
        )}&endLng=${encodeURIComponent(endLng)}&mode=${encodeURIComponent(mode)}`
      );
      const userRoutesList = await response.json();

      const contributed = [];
      for (const contrib of userRoutesList) {
        const coords = contrib.coordinates;
        let fullGeometry = [];
        let allSteps = [];

        for (let i = 0; i < coords.length - 1; i++) {
          const src = coords[i];
          const dst = coords[i + 1];

          const segRes = await fetch(
            `http://localhost:8080/locations/getRoutePath?source=${src[0]},${src[1]}&destination=${dst[0]},${dst[1]}&mode=${mode}`
          );
          const segData = await segRes.json();

          if (segData.geometry) fullGeometry.push(...polyline.decode(segData.geometry));
          if (Array.isArray(segData.steps)) allSteps.push(...segData.steps);
        }

        contributed.push({
          geometry: fullGeometry,
          nodes: [
            { id: "start", name: "Start", latitude: coords[0][0], longitude: coords[0][1] },
            { id: "end", name: "End", latitude: coords[coords.length - 1][0], longitude: coords[coords.length - 1][1] },
          ],
          description: `${contrib.description} (by ${contrib.username})`,
          rating: null,
          userCount: null,
          distance: null,
          duration: null,
          steps: allSteps,
        });
      }

      return contributed;
    } catch (err) {
      console.error("Failed to load user-contributed routes:", err);
      return [];
    }
  };

  // fetchRoutes 
  const fetchRoutes = async (modeArg, overrides = {}) => {
    const mode = modeArg ?? transportMode;

    const fromVal = (overrides.from ?? from) || "";
    const toVal   = (overrides.to   ?? to)   || "";

    setLoading(true);
    setShowFeedbackSection(false);
    setExpandedIndex(null);
    setError("");
    try {
      const res1 = await fetch(
        `http://localhost:8080/locations/getRoutePath?source=${encodeURIComponent(fromVal)}&destination=${encodeURIComponent(toVal)}&mode=${mode}`
      );
      const data1 = await res1.json();

      if (!data1.geometry) {
        setError("No valid path found.");
        setRoutes([]);
        setHighlightedRouteIndex(null);
        setLoading(false);
        return;
      }

      const geometry1 = polyline.decode(data1.geometry);
      setDirections(data1.steps || []);
      const nodes1 = [
        { id: "start", name: data1.startLocation, latitude: geometry1[0][0], longitude: geometry1[0][1] },
        { id: "end", name: data1.endLocation, latitude: geometry1[geometry1.length - 1][0], longitude: geometry1[geometry1.length - 1][1] },
      ];

      const allRoutes = [
        {
          geometry: geometry1,
          nodes: nodes1,
          description: "Shortest Route",
          rating: null,
          userCount: null,
          distance: data1.distance,
          duration: data1.duration,
          steps: data1.steps || [],
        },
      ];

      let alternateRoutes = [];
      try {
        const res2 = await fetch(
          `http://localhost:8080/locations/allRoutes?from=${encodeURIComponent(fromVal)}&to=${encodeURIComponent(toVal)}`
        );
        const data2 = await res2.json();

        if (Array.isArray(data2) && data2.length > 0) {
          for (const route of data2) {
            let fullGeometry = [];

            for (let i = 0; i < route.nodes.length - 1; i++) {
              const src = route.nodes[i].name;
              const dst = route.nodes[i + 1].name;

              const segRes = await fetch(
                `http://localhost:8080/locations/getRoutePath?source=${encodeURIComponent(src)}&destination=${encodeURIComponent(dst)}`
              );
              const segData = await segRes.json();
              if (segData.geometry) {
                const decodedSegment = polyline.decode(segData.geometry);
                fullGeometry.push(...decodedSegment);
              }
            }

            const routeNodes = route.nodes.map((node) => ({
              id: node.id,
              name: node.name,
              latitude: node.lat,
              longitude: node.lng,
            }));

            alternateRoutes.push({
              geometry: fullGeometry,
              nodes: routeNodes,
              description: route.description,
              rating: route.rating,
              userCount: route.userCount ?? null,
              distance: null,
              duration: null,
            });
          }
        }
      } catch {
        // no alternates available; 
      }

      const userRoutes = await fetchUserContributedRoutes(fromVal, toVal, mode);
      const finalRoutes = [...allRoutes, ...alternateRoutes, ...userRoutes];

      setRoutes(finalRoutes);
      setHighlightedRouteIndex(0);
      setError("");

      //fly to the primary (shortest) route
      flyToRouteGeometry(geometry1);
    } catch (err) {
      console.error(err);
      setError("Failed to fetch route data.");
      setRoutes([]);
      setHighlightedRouteIndex(null);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitFeedback = async () => {
    try {
      const userId = parseInt(localStorage.getItem("userId"));
      const currentRoute = routes[highlightedRouteIndex];

      if (isRouteVia(currentRoute)) {
        alert("Feedback is disabled for via-stop routes.");
        return;
      }

      const routeType = currentRoute?.description === "Shortest Route" ? "EXTERNAL" : "INTERNAL";

      const normalRes = await fetch("http://localhost:8080/locations/ExternalRouteId", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ startLocation: from, endLocation: to, transportMode, routeType }),
      });
      if (!normalRes.ok) throw new Error("Failed to get route ID");
      const routeId = await normalRes.json();

      const feedbackRes = await fetch("http://localhost:8080/locations/submitFeedback", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ routeId, userId, rating: parseInt(feedbackRating), comment: feedbackComment, routeType }),
      });

      if (!feedbackRes.ok) throw new Error("Failed to submit feedback");

      alert("Feedback submitted successfully!");
      setFeedbackRating("");
      setFeedbackComment("");
      setShowFeedbackSection(false);

      if (expandedIndex !== null) handleRouteClick(expandedIndex);
    } catch (err) {
      console.error("Error submitting feedback:", err);
      alert("An error occurred while submitting feedback.");
    }
  };

  const handleRouteClick = async (index) => {
    const route = routes[index];
    if (isRouteVia(route)) return;

    setHighlightedRouteIndex(index);
    setExpandedIndex(index);
    setDirections(routes[index]?.steps || []);
    setShowFeedbackSection(false);

    //fly to the clicked route
    flyToRouteGeometry(route?.geometry);

    try {
      const routeType = route?.description === "Shortest Route" ? "EXTERNAL" : "INTERNAL";

      const res = await fetch("http://localhost:8080/locations/ExternalRouteId", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ startLocation: from, endLocation: to, transportMode, routeType }),
      });
      if (!res.ok) throw new Error("Failed to get route id");
      const routeId = await res.json();

      const fb = await fetch(
        `http://localhost:8080/locations/feedbacks?routeType=${routeType}&routeId=${routeId}`
      );
      if (!fb.ok) throw new Error("Failed to fetch reviews");
      const data = await fb.json();
      setRouteFeedbacks(data);
    } catch (error) {
      console.error("Error fetching route reviews:", error);
    }
  };

  const getCurrentLocation = () => {
    if (!navigator.geolocation) {
      alert("Geolocation is not supported by your browser");
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const lat = position.coords.latitude;
        const lon = position.coords.longitude;
        setUserLocation({ lat, lon });
        setFrom(`${lat},${lon}`);
      },
      () => alert("Unable to retrieve your location")
    );
  };

  const handleSaveProfile = () => {
    const userId = localStorage.getItem("userId");
    fetch("http://localhost:8080/userMgmt/updateUserDetails", {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ userId: parseInt(userId), phoneNumber, email, bio, dob }),
    })
      .then((res) => {
        if (!res.ok) throw new Error("Failed to update profile");
        return res.json();
      })
      .then(() => {
        alert("Profile updated successfully");
        setIsEditable(false);
      })
      .catch((err) => {
        console.error(err);
        alert("Failed to save profile");
      });
  };

  // VIA ROUTE ID HELPER
  const getExternalViaRouteId = async () => {
    const res = await fetch("http://localhost:8080/locations/ExternalViaRouteId", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        startLocation: from,
        viaStops: viaStops.filter((s) => s && s.trim() !== ""),
        endLocation: to,
        transportMode,
      }),
    });
    if (!res.ok) throw new Error("Failed to get ExternalViaRouteId");
    return res.json();
  };

  // SAVE FEATURE
  const openSaveModal = (idx) => {
    setSaveModalIdx(idx);
    setSaveName("");
    setSaveDescription("");
    setShowSaveModal(true);
  };

  const saveCurrentRoute = async () => {
    if (saveModalIdx === null) return;
    const userId = parseInt(localStorage.getItem("userId"));
    const route = routes[saveModalIdx];
    if (!route) return;

    const routeIsVia = isRouteVia(route);
    const routeType = routeIsVia ? "EXTERNALVIA" : route?.description === "Shortest Route" ? "EXTERNAL" : "INTERNAL";

    try {
      setSavingRoute(true);

      let routeId;
      if (routeIsVia) {
        routeId = await getExternalViaRouteId();
      } else {
        const idRes = await fetch("http://localhost:8080/locations/ExternalRouteId", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ startLocation: from, endLocation: to, transportMode, routeType }),
        });
        if (!idRes.ok) throw new Error("Failed to get routeId");
        routeId = await idRes.json();
      }

      const saveRes = await fetch("http://localhost:8080/userRouteSave/saveUserRoute", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          userId,
          routeId,
          routeType,
          name:
            saveName ||
            (routeIsVia
              ? "Saved Route (via stops)"
              : routeType === "EXTERNAL"
              ? "Saved Shortest Route"
              : "Saved Contributed Route"),
          description: saveDescription || "",
        }),
      });

      if (!saveRes.ok) {
        const msg = await saveRes.text();
        throw new Error(msg || "Failed to save route");
      }

      await fetchSavedRoutes();

      setShowSaveModal(false);
      setSaveModalIdx(null);
      setSaveName("");
      setSaveDescription("");
      alert("Route saved!");
    } catch (e) {
      console.error(e);
      alert(e.message || "Could not save route");
    } finally {
      setSavingRoute(false);
    }
  };

  const fetchSavedRoutes = async () => {
    const userId = parseInt(localStorage.getItem("userId"));
    if (!userId) return;
    try {
      const res = await fetch(
        `http://localhost:8080/userRouteSave/getAllUserSavedRoutes?userId=${userId}`
      );
      if (!res.ok) throw new Error("Failed to fetch saved routes");
      const data = await res.json();
      setSavedRoutes(data || []);
    } catch (e) {
      console.error(e);
      setSavedRoutes([]);
    }
  };

  const openSavedOverlay = async () => {
    await fetchSavedRoutes();
    setShowSavedOverlay(true);
  };
  const closeSavedOverlay = () => setShowSavedOverlay(false);

  // History controls
  const fetchUserHistory = async () => {
    const uid = parseInt(localStorage.getItem("userId"));
    if (!uid) {
      setHistoryItems([]);
      setHistoryError("No userId in localStorage");
      return;
    }
    setLoadingHistory(true);
    setHistoryError("");
    try {
      const res = await fetch(`http://localhost:8080/userRouteSave/userActivity?userId=${uid}`);
      if (!res.ok) throw new Error(await res.text());
      const data = await res.json();
      setHistoryItems(Array.isArray(data) ? data : []);
    } catch (e) {
      console.error("userActivity error:", e);
      setHistoryError("Failed to load activity.");
      setHistoryItems([]);
    } finally {
      setLoadingHistory(false);
    }
  };
  const openHistoryOverlay = async () => {
    setShowProfileOverlay(false);
    setShowSavedOverlay(false);
    await fetchUserHistory();
    setShowHistoryOverlay(true);
  };
  const closeHistoryOverlay = () => setShowHistoryOverlay(false);

  const loadSavedRouteOnMap = async (saved) => {
    try {
      const url = `http://localhost:8080/userRouteSave/listOfSavedRoutes?userId=${saved.userId}&savedId=${saved.id}&routeType=${encodeURIComponent(
        saved.routeType
      )}`;
      const res = await fetch(url);
      if (!res.ok) {
        const txt = await res.text();
        console.error("listOfSavedRoutes failed:", res.status, txt);
        throw new Error("Failed to fetch saved route details");
      }
      const data = await res.json();

      const decoded = data.geometry ? polyline.decode(data.geometry) : [];
      const startArr = JSON.parse(data.startLocation);
      const endArr = JSON.parse(data.endLocation);

      const builtRoute = {
        geometry: decoded,
        nodes: [
          { id: "start", name: data.startLocation, latitude: startArr[0], longitude: startArr[1] },
          { id: "end", name: data.endLocation, latitude: endArr[0], longitude: endArr[1] },
        ],
        description: saved.name || "Saved Route",
        distance: data.distance ?? null,
        duration: data.duration ?? null,
        steps: data.steps || [],
      };

      setRoutes([builtRoute]);
      setHighlightedRouteIndex(0);
      setDirections(builtRoute.steps || []);
      setExpandedIndex(null);
      setShowFeedbackSection(false);
      setError("");
      setActiveSavedId(saved.id);

      //fly to saved route
      flyToRouteGeometry(decoded);
    } catch (e) {
      console.error(e);
      alert("Unable to open saved route");
    }
  };

  //chat core handlers
  const applyIntentToUI = (intent) => {
    if (!intent) return;
    if (intent.useCurrentLocation) {
      // will ask for location
    } else if (intent.from) {
      setFrom(intent.from);
    }
    if (intent.to) setTo(intent.to);
    if (intent.mode) setTransportMode(intent.mode);
    if (Array.isArray(intent.via)) setViaStops(intent.via);
  };

  const sendToNlp = async (prompt) => {
    if (!prompt || !prompt.trim()) return;

    setLastPrompt(prompt);
    setChatMsgs((m) => [...m, { id: cryptoId(), role: "user", text: prompt }]);
    setChatInput("");

    const context = { country: "UK" };
    if (userLocation) {
      context.userLat = userLocation.lat;
      context.userLon = userLocation.lon;
    }

    try {
      const res = await fetch("http://localhost:8080/nlp/parse", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ prompt, context }),
      });
      const payload = await res.json();

      const intent = payload?.data;
      const ask = payload?.ask;
      const nextAction = payload?.nextAction;
      const cta = payload?.cta;

      if (intent) applyIntentToUI(intent);

      if (ask) {
        setChatMsgs((m) => [...m, { id: cryptoId(), role: "assistant", text: ask }]);
      } else if (nextAction === "SHOW_ROUTE") {
        setChatMsgs((m) => [...m, { id: cryptoId(), role: "assistant", text: "Here’s the route on the map." }]);
      }

      if (cta && cta.type === "requestLocation") setShowLocationCTA(true);
      else setShowLocationCTA(false);


      if (intent?.via) setViaStops(normalizeVia(intent.via));
      if (!intent?.useCurrentLocation && intent?.from) setFrom(intent.from);
      if (intent?.to) setTo(intent.to);
      if (intent?.mode) setTransportMode(intent.mode);

      if (nextAction === "SHOW_ROUTE") {
        if (intent?.useCurrentLocation && !userLocation) {
          setShowLocationCTA(true);
          return;
        }

        const viaArr = normalizeVia(intent?.via);
        const overrides = {
          from: intent?.useCurrentLocation
            ? `${userLocation.lat},${userLocation.lon}`
            : (intent?.from ?? from),
          to: intent?.to ?? to,
          via: viaArr,
        };
        const mode = intent?.mode ?? transportMode;

        if (viaArr.length > 0) {
          await fetchRoutesWithVia(mode, overrides);
        } else {
          await fetchRoutes(mode, overrides);
        }
      }
      // -------------------------------------------------------
    } catch (e) {
      console.error("NLP error:", e);
      setChatMsgs((m) => [
        ...m,
        { id: cryptoId(), role: "assistant", text: "Sorry, I couldn’t understand that. Try rephrasing your trip." },
      ]);
    }
  };

  const handleDeleteSaved = async (sr) => {
    try {
      const userId = parseInt(localStorage.getItem("userId"));
      if (!userId) {
        alert("Missing userId.");
        return;
      }
      const ok = window.confirm(`Delete "${sr.name || "this route"}"?`);
      if (!ok) return;

      const url = `http://localhost:8080/userRouteSave/deleteUserRoute?savedId=${sr.id}&userId=${userId}`;
      let res = await fetch(url, { method: "DELETE" });
      if (!res.ok) res = await fetch(url);

      if (!res.ok) {
        const msg = await res.text();
        throw new Error(msg || "Failed to delete saved route");
      }

      if (activeSaveId === sr.id) setActiveSavedId(null);
      await fetchSavedRoutes();
      alert("Deleted.");
    } catch (e) {
      console.error(e);
      alert(e.message || "Could not delete saved route");
    }
  };

  const handleImHere = () => {
    if (!navigator.geolocation) {
      setChatMsgs((m) => [
        ...m,
        { id: cryptoId(), role: "assistant", text: "Geolocation not supported by your browser." },
      ]);
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const lat = position.coords.latitude;
        const lon = position.coords.longitude;
        setUserLocation({ lat, lon });
        setFrom(`${lat},${lon}`);
        setShowLocationCTA(false);
        setChatMsgs((m) => [
          ...m,
          { id: cryptoId(), role: "assistant", text: "Got your location. Re-checking your request…" },
        ]);
        if (lastPrompt) sendToNlp(lastPrompt);
      },
      () => {
        setChatMsgs((m) => [
          ...m,
          { id: cryptoId(), role: "assistant", text: "Couldn’t get your location. Please try again." },
        ]);
      },
      { enableHighAccuracy: true, timeout: 10000 }
    );
  };
  // =========================================

  // ---------- UI ----------
  return (
    <div className="route-container">
      <div className="left-panel">
        {!showDrawer && (
          <button className="hamburger-button" onClick={() => setShowDrawer(true)}>☰</button>
        )}

        {!showDrawer && (
          <div style={{ position: "absolute", top: 20, right: 10, zIndex: 1100 }}>
            <button
              onClick={() => navigate("/contribute")}
              style={{ backgroundColor: "#8e44ad", color: "white", border: "none", borderRadius: "5px", padding: "8px 14px", cursor: "pointer", fontSize: "14px" }}
            >
              ➕ Contribute Route
            </button>
          </div>
        )}

        {showDrawer && !showProfileOverlay && !showSavedOverlay && !showHistoryOverlay && (
          <div className="drawer-overlay">
            <button className="close-drawer" onClick={() => setShowDrawer(false)}>×</button>
            <div className="drawer-content">
                  {/* Header: emoji + username + email */}
            <div className="drawer-header">
            <div className="drawer-avatar">
            {(fullName || userData.userName || email || "U").charAt(0).toUpperCase()}
            </div>

            <div className="drawer-user">
            <div className="drawer-name">
              {userData.userName || fullName || "User"}
              </div>

              {(email || userData.email) && (
              <div className="drawer-email" title={email || userData.email}>
              {email || userData.email}
              </div>
              )}
              </div>
              </div>
              <hr className="drawer-divider" />
              <div className="drawer-section" onClick={() => setShowProfileOverlay(true)}>👤 Profile</div>
              <div className="drawer-section" onClick={openHistoryOverlay}>🕘 History</div>
              <div className="drawer-section" onClick={openSavedOverlay}>🛣️ Saved Routes</div>
              <div style={{ flex: 1 }} />
              <button
                className="logout-button"
                onClick={() => {
                  localStorage.clear();
                  navigate("/welcome");
                }}
              >
                Log Out
              </button>
            </div>
          </div>
        )}

        {showProfileOverlay && (
          <div className="drawer-overlay">
            <button className="close-drawer" onClick={() => setShowProfileOverlay(false)}>×</button>
            <h2 className="profile-title">User Profile</h2>
            <div className="profile-form">
              <label>Full Name</label>
              <input type="text" value={fullName} onChange={(e) => setFullName(e.target.value)} disabled={!isEditable} />
              <label>Phone Number</label>
              <input type="text" value={phoneNumber} onChange={(e) => setPhoneNumber(e.target.value)} disabled={!isEditable} />
              <label>Email</label>
              <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} disabled={!isEditable} />
              <label>Bio</label>
              <textarea value={bio} onChange={(e) => setBio(e.target.value)} rows="4" disabled={!isEditable} />
              <label>Date Of Birth</label>
              <input type="date" value={dob} onChange={(e) => setDob(e.target.value)} disabled={!isEditable} />
              {!isEditable ? (
                <button className="edit-button" onClick={() => setIsEditable(true)}>Edit</button>
              ) : (
                <button className="save-button" onClick={handleSaveProfile}>Save</button>
              )}
            </div>
          </div>
        )}

        {/* Saved Routes */}
        {showSavedOverlay && (
          <div className="drawer-overlay" style={{ padding: "10px" }}>
            <div style={{ position: "relative", marginBottom: "10px" }}>
              <h2 className="profile-title" style={{ margin: 0, fontSize: "18px", fontWeight: 700, textAlign: "center" }}>Your Saved Routes</h2>
              <button className="close-drawer" onClick={closeSavedOverlay}
                style={{ background: "none", border: "none", color: "#000", fontSize: "20px", fontWeight: "bold", cursor: "pointer", padding: "8px 12px", lineHeight: "1", position: "absolute", top: "-6px", left: "8px" }}>
                ×
              </button>
            </div>

            {savedRoutes.length === 0 ? (
              <p style={{ textAlign: "center", color: "#777", marginTop: 20 }}>No saved routes yet.</p>
            ) : (
              <div style={{ display: "grid", gridTemplateColumns: "1fr", gap: "10px", paddingBottom: "5px" }}>
                {savedRoutes.map((sr) => (
                  <div
                    key={sr.id}
                    style={{
                      background: "#fff",
                      border: "1px solid #e9e9ef",
                      borderLeft: activeSaveId === sr.id ? "4px solid #ab1be8" : "4px solid #8e44ad",
                      borderRadius: "8px",
                      padding: "10px",
                      boxShadow: "0 2px 6px rgba(0,0,0,0.05)",
                      cursor: "pointer",
                      transition: "box-shadow .2s, transform .06s",
                    }}
                    onClick={() => loadSavedRouteOnMap(sr)}
                    onMouseEnter={(e) => {
                      e.currentTarget.style.boxShadow = "0 6px 16px rgba(0,0,0,0.10)";
                      e.currentTarget.style.transform = "translateY(-1px)";
                    }}
                    onMouseLeave={(e) => {
                      e.currentTarget.style.boxShadow = "0 2px 6px rgba(0,0,0,0.05)";
                      e.currentTarget.style.transform = "translateY(0)";
                    }}
                  >
                    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", gap: 8 }}>
                      <strong style={{ fontSize: "14px", flex: 1, minWidth: 0, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }} title={sr.name}>
                        {sr.name}
                      </strong>

                      <div style={{ display: "flex", alignItems: "center", gap: 6 }}>
                        <span style={{ fontSize: "11px", fontWeight: 700, background: "#F3E8FF", color: "#6B21A8", border: "1px solid #E9D5FF", padding: "2px 6px", borderRadius: 999, whiteSpace: "nowrap" }}>
                          {sr.routeType}
                        </span>
                        <button
                          title="Delete saved route"
                          onClick={(e) => {
                            e.stopPropagation();
                            handleDeleteSaved(sr);
                          }}
                          style={{ border: "none", cursor: "pointer", fontSize: 16, lineHeight: 1, padding: 4, borderRadius: 6, backgroundColor: "transparent", color: "red" }}
                          onMouseEnter={(e) => (e.currentTarget.style.background = "#f4f4f6")}
                          onMouseLeave={(e) => (e.currentTarget.style.background = "transparent")}
                        >
                          🗑
                        </button>
                      </div>
                    </div>

                    <p style={{ margin: "4px 0 6px", color: "#555", fontSize: "13px" }}>{sr.description || "-"}</p>

                    {sr.savedAt && (
                      <div style={{ fontSize: "11px", color: "#8a8a98", display: "flex", alignItems: "center", gap: 5 }}>
                        <span>🕒</span>
                        <span>
                          {new Date(sr.savedAt).toLocaleString(undefined, {
                            year: "numeric",
                            month: "short",
                            day: "2-digit",
                            hour: "2-digit",
                            minute: "2-digit",
                          })}
                        </span>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* History */}
        {showHistoryOverlay && (
          <div className="drawer-overlay" style={{ padding: "10px" }}>
            <div style={{ position: "relative", marginBottom: "10px" }}>
              <h2 className="profile-title" style={{ margin: 0, fontSize: "18px", fontWeight: 700, textAlign: "center" }}>Your Activity</h2>
              <button className="close-drawer" onClick={closeHistoryOverlay}
                style={{ background: "none", border: "none", color: "#000", fontSize: "20px", fontWeight: "bold", cursor: "pointer", padding: "8px 12px", lineHeight: "1", position: "absolute", top: "-6px", left: "8px" }}>
                ×
              </button>
            </div>

            {loadingHistory ? (
              <p style={{ textAlign: "center", color: "#777", marginTop: 20 }}>Loading…</p>
            ) : historyError ? (
              <p style={{ textAlign: "center", color: "crimson", marginTop: 20 }}>{historyError}</p>
            ) : historyItems.length === 0 ? (
              <p style={{ textAlign: "center", color: "#777", marginTop: 20 }}>No activity yet.</p>
            ) : (
              <div style={{ display: "grid", gridTemplateColumns: "1fr", gap: "10px", paddingBottom: "5px" }}>
                {historyItems.map((it) => (
                  <div
                    key={it.id}
                    style={{
                      background: "#fff",
                      border: "1px solid #e9e9ef",
                      borderLeft: "4px solid #ab1be8",
                      borderRadius: "8px",
                      padding: "10px",
                      boxShadow: "0 2px 6px rgba(0,0,0,0.05)",
                      transition: "box-shadow .2s, transform .06s",
                    }}
                    onMouseEnter={(e) => {
                      e.currentTarget.style.boxShadow = "0 6px 16px rgba(0,0,0,0.10)";
                      e.currentTarget.style.transform = "translateY(-1px)";
                    }}
                    onMouseLeave={(e) => {
                      e.currentTarget.style.boxShadow = "0 2px 6px rgba(0,0,0,0.05)";
                      e.currentTarget.style.transform = "translateY(0)";
                    }}
                  >
                    <div style={{ display: "flex", alignItems: "flex-start", justifyContent: "space-between", gap: 8 }}>
                      <div style={{ display: "flex", gap: 8, alignItems: "center", flex: 1, minWidth: 0 }}>
                        <span style={{ fontSize: 18 }}>{actionIcon(it.action)}</span>
                        <div style={{ minWidth: 0 }}>
                          <div style={{ fontWeight: 600, fontSize: 14, whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis" }} title={it.title}>
                            {it.title}
                          </div>
                          <div style={{ fontSize: 11, color: "#8a8a98" }}>{timeAgo(it.time)}</div>
                        </div>
                      </div>
                      <span
                        style={{ fontSize: 11, fontWeight: 700, background: "#F3E8FF", color: "#6B21A8", border: "1px solid #E9D5FF", padding: "2px 6px", borderRadius: 999, whiteSpace: "nowrap" }}
                        title={it.action}
                      >
                        {it.action}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {!showDrawer && (
          <>
            <div className="floating-card">
              <h2>Driving directions</h2>
              <form
                onSubmit={(e) => {
                  e.preventDefault();
                  const mode = transportMode;
                  if (isViaMode) fetchRoutesWithVia(mode);
                  else fetchRoutes(mode);
                  setShowModes(true);
                }}
              >
                <div className="form-group" style={{ display: "flex", alignItems: "center" }}>
                  <span className="icon">📍</span>

                  <div style={{ display: "flex", flex: 1 }}>
                    <input
                      type="text"
                      placeholder="Choose starting point"
                      value={from}
                      onChange={(e) => setFrom(e.target.value)}
                      required
                      style={{ flex: 1, borderTopRightRadius: 0, borderBottomRightRadius: 0 }}
                    />
                    <button
                      type="button"
                      onClick={() => setViaStops([...viaStops, ""])}
                      title="Add via stops"
                      style={{
                        background: "#fff",
                        border: "1px solid #ccc",
                        borderLeft: "none",
                        borderTopRightRadius: "4px",
                        borderBottomRightRadius: "4px",
                        padding: "0 12px",
                        fontSize: "18px",
                        cursor: "pointer",
                      }}
                    >
                      +
                    </button>
                  </div>

                  <button
                    type="button"
                    className="use-location-btn"
                    onClick={getCurrentLocation}
                    style={{ backgroundColor: "#8e44ad", color: "white", border: "none", borderRadius: "20px", padding: "6px 12px", cursor: "pointer", marginLeft: "8px" }}
                  >
                    I'm Here
                  </button>
                </div>

                {/* via stops */}
                {viaStops.map((stop, idx) => (
                  <div className="form-group" key={idx}>
                    <span className="icon">🚩</span>
                    <input type="text" placeholder={`Via Stop ${idx + 1}`} value={stop} onChange={(e) => handleViaStopChange(idx, e.target.value)} />
                    <button type="button" onClick={() => handleRemoveViaStop(idx)} style={{ marginLeft: "5px", background: "red", color: "white", border: "none", cursor: "pointer" }}>
                      ×
                    </button>
                  </div>
                ))}

                <div className="form-group">
                  <span className="icon">🎯</span>
                  <input type="text" placeholder="Choose destination" value={to} onChange={(e) => setTo(e.target.value)} required />
                </div>

                <button type="submit" className="submit-btn">Show Route</button>
                {showModes && (
                  <div className="transport-modes" style={{ marginTop: "1px", fontSize: "20px", display: "flex", justifyContent: "center", gap: "50px" }}>
                    <button type="button" onClick={() => handleModeChange("foot-walking")} title="Walking" style={{ fontSize: "20px", background: transportMode === "foot-walking" ? "#e0e0e0" : "none", border: "none" }}>
                      🚶
                    </button>
                    <button type="button" onClick={() => handleModeChange("cycling-regular")} title="Cycling" style={{ fontSize: "20px", background: transportMode === "cycling-regular" ? "#e0e0e0" : "none", border: "none" }}>
                      🚴
                    </button>
                    <button type="button" onClick={() => handleModeChange("driving-car")} title="Car" style={{ fontSize: "20px", background: transportMode === "driving-car" ? "#e0e0e0" : "none", border: "none" }}>
                      🚗
                    </button>
                    <button type="button" onClick={() => handleModeChange("driving-hgv")} title="Truck" style={{ fontSize: "20px", background: transportMode === "driving-hgv" ? "#e0e0e0" : "none", border: "none" }}>
                      🚚
                    </button>
                  </div>
                )}
              </form>
            </div>

            {loading ? (
              <div className="loading-message">Loading routes...</div>
            ) : (
              routes.length > 0 && (
                <div className="routes-list-container" style={{ padding: "10px" }}>
                  <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "10px" }}>
                    {expandedIndex !== null && (
                      <button
                        onClick={() => {
                          setExpandedIndex(null);
                          setShowFeedbackSection(false);
                        }}
                        style={{ background: "#f5f5f5", border: "1px solid #ccc", padding: "5px 12px", borderRadius: "5px", cursor: "pointer" }}
                      >
                        ← Back
                      </button>
                    )}
                    <h3 style={{ margin: 0, textAlign: "center", width: "100%" }}>
                      {expandedIndex === null ? "Available Routes" : "User Reviews"}
                    </h3>

                    {expandedIndex !== null && !isRouteVia(routes[highlightedRouteIndex]) && (
                      <button
                        style={{ backgroundColor: "#8e44ad", color: "white", border: "none", padding: "6px 14px", borderRadius: "5px", cursor: "pointer" }}
                        onClick={() => setShowFeedbackSection(true)}
                      >
                        ➕ Add
                      </button>
                    )}
                  </div>

                  {expandedIndex === null ? (
                    <div style={{ display: "flex", flexDirection: "column", gap: "8px" }}>
                      {routes.map((route, idx) => {
                        const isVia = isRouteVia(route);
                        return (
                          <div key={idx} style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                            <button
                              onClick={(e) => {
                                if (isVia) {
                                  e.preventDefault();
                                  e.stopPropagation();
                                  return;
                                }
                                handleRouteClick(idx);
                              }}
                              className={`route-button ${highlightedRouteIndex === idx ? "active" : ""}`}
                              style={{
                                padding: "8px",
                                border: "1px solid #ddd",
                                borderRadius: "6px",
                                background: highlightedRouteIndex === idx ? "#f0e6ff" : "white",
                                cursor: isVia ? "default" : "pointer",
                                textAlign: "left",
                                display: "flex",
                                justifyContent: "space-between",
                                alignItems: "center",
                                flex: 1,
                              }}
                            >
                              <strong>{route.description}</strong>
                            </button>

                            <button
                              type="button"
                              onClick={() => openSaveModal(idx)}
                              style={{ backgroundColor: "#3f0857ff", color: "#fff", border: "none", borderRadius: "6px", padding: "6px 10px", cursor: "pointer", whiteSpace: "nowrap" }}
                              title="Save this route"
                            >
                              💾 Save
                            </button>
                          </div>
                        );
                      })}
                    </div>
                  ) : (
                    <div>
                      {routeFeedbacks.length > 0 ? (
                        <div className="route-reviews" style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
                          {routeFeedbacks.map((review, i) => (
                            <div key={i} style={{ background: "#fff", border: "1px solid #ddd", borderRadius: "8px", padding: "10px", boxShadow: "0 2px 6px rgba(0,0,0,0.05)" }}>
                              <p style={{ margin: "0 0 4px" }}><strong>{review.userName}</strong></p>
                              <p style={{ margin: "0 0 4px", color: "#ff9800" }}>⭐ {review.rating}</p>
                              <p style={{ margin: 0, fontStyle: "italic" }}>{review.comment}</p>
                            </div>
                          ))}
                        </div>
                      ) : (
                        <p style={{ textAlign: "center", color: "#777" }}>No reviews yet.</p>
                      )}
                    </div>
                  )}
                </div>
              )
            )}

            {error && <p className="error-message">{error}</p>}
          </>
        )}
      </div>

      <div className="map">
        <MapContainer
          center={[52.6369, -1.1398]}
          zoom={6}
          scrollWheelZoom
          style={{ height: "100%", width: "100%" }}
          // capture map instance
          whenCreated={(map) => (mapRef.current = map)}
        >
          <TileLayer attribution="&copy; OpenStreetMap contributors" url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
          {routes.map(
            (route, idx) =>
              highlightedRouteIndex === idx && (
                <Polyline
                  key={`route-${idx}`}
                  positions={route.geometry}
                  pathOptions={{ color: colors[idx % colors.length], weight: 5, opacity: 1 }}
                >
                  <Popup>
                    <strong>{route.description}</strong>
                    <br />
                    {route.distance && `Distance: ${route.distance} miles`}
                    <br />
                    {route.duration && `Duration: ${route.duration} hrs`}
                  </Popup>
                </Polyline>
              )
          )}

          {routes[highlightedRouteIndex]?.nodes?.map((node, idx) => (
            <Marker key={`marker-${idx}`} position={[node.latitude, node.longitude]} icon={customIcon}>
              <Popup>{node.name}</Popup>
            </Marker>
          ))}

          {userLocation && (
            <Marker position={[userLocation.lat, userLocation.lon]} icon={userLocationIcon}>
              <Popup>Your Current Location</Popup>
            </Marker>
          )}


            <AutoZoom
    geometry={
      (highlightedRouteIndex != null
        ? routes[highlightedRouteIndex]
        : routes[0]
      )?.geometry || []
    }
  />
        </MapContainer>
      </div>

      {directions.length > 0 && (
        <div
          className="floating-card"
          style={{ position: "absolute", top: 20, right: 20, width: "300px", background: "white", padding: "10px", borderRadius: "10px", boxShadow: "0 0 10px rgba(0,0,0,0.1)", maxHeight: "50vh", overflowY: "auto", zIndex: 1000 }}
        >
          <h4 style={{ marginTop: 0 }}>🧭 Step-by-Step Directions</h4>
          <ol style={{ paddingLeft: "20px" }}>
            {directions.map((step, idx) => (
              <li key={idx} style={{ marginBottom: "8px", backgroundColor: idx === currentStepIndex ? "#e0f7fa" : "transparent", padding: "5px", borderRadius: "5px" }}>
                {getDirectionIcon(step.instruction)} {step.instruction || "Unnamed step"} <br />
                <small>{step.distance} meters</small>
              </li>
            ))}
          </ol>
        </div>
      )}

      {/* Feedback form */}
      {showFeedbackSection && !isRouteVia(routes[highlightedRouteIndex]) && (
        <div
          className="feedback-form"
          style={{ width: "360px", position: "absolute", bottom: 30, left: "50%", transform: "translateX(-50%)", background: "#fff", padding: "20px", borderRadius: "12px", boxShadow: "0 4px 20px rgba(0,0,0,0.15)", textAlign: "center", zIndex: 2000 }}
        >
          <h4 style={{ marginBottom: "12px", color: "#333" }}>Leave Feedback for this Route</h4>

          <div className="star-rating" style={{ fontSize: "26px", marginBottom: "12px", color: "#FFD700" }}>
            {[1, 2, 3, 4, 5].map((star) => (
              <span key={star} onMouseEnter={() => setHoverRating(star)} onMouseLeave={() => setHoverRating(0)} onClick={() => setFeedbackRating(star)} style={{ cursor: "pointer", padding: "0 4px", transition: "transform 0.2s" }}>
                {(hoverRating || feedbackRating) >= star ? "⭐" : "☆"}
              </span>
            ))}
          </div>

          <textarea
            rows="3"
            placeholder="Your comment..."
            value={feedbackComment}
            onChange={(e) => setFeedbackComment(e.target.value)}
            style={{ width: "100%", padding: "10px", borderRadius: "8px", border: "1px solid #ccc", outline: "none", marginBottom: "12px", fontSize: "14px", resize: "none" }}
          />

          <div style={{ display: "flex", justifyContent: "center", gap: "10px" }}>
            <button onClick={handleSubmitFeedback} style={{ backgroundColor: "#28a745", color: "white", border: "none", borderRadius: "8px", padding: "8px 16px", cursor: "pointer", fontSize: "14px" }}>
              Submit
            </button>
            <button onClick={() => setShowFeedbackSection(false)} style={{ backgroundColor: "#dc3545", color: "white", border: "none", borderRadius: "8px", padding: "8px 16px", cursor: "pointer", fontSize: "14px" }}>
              Cancel
            </button>
          </div>
        </div>
      )}

      {/* Save Modal */}
      {showSaveModal && (
        <div
          style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.35)", display: "flex", alignItems: "center", justifyContent: "center", zIndex: 3000 }}
          onClick={() => { if (!savingRoute) setShowSaveModal(false); }}
        >
          <div
            style={{ background: "#fff", width: 420, maxWidth: "92vw", borderRadius: 12, padding: 16, boxShadow: "0 10px 30px rgba(0,0,0,0.25)" }}
            onClick={(e) => e.stopPropagation()}
          >
            <h3 style={{ marginTop: 0, textAlign: "center" }}>Save this route</h3>
            <label style={{ fontWeight: 600, display: "block", marginBottom: 6 }}>Name</label>
            <input type="text" value={saveName} onChange={(e) => setSaveName(e.target.value)} placeholder="e.g., Morning Walk to Park" style={{ width: "95%", padding: "10px", borderRadius: 8, border: "1px solid #ccc", marginBottom: 10 }} />
            <label style={{ fontWeight: 600, display: "block", marginBottom: 6 }}>Description</label>
            <textarea rows={3} value={saveDescription} onChange={(e) => setSaveDescription(e.target.value)} placeholder="Short note about this route..." style={{ width: "95%", padding: "10px", borderRadius: 8, border: "1px solid #ccc", marginBottom: 14, resize: "none" }} />
            <div style={{ display: "flex", justifyContent: "flex-end", gap: 10 }}>
              <button onClick={() => setShowSaveModal(false)} disabled={savingRoute} style={{ background: "#eee", border: "1px solid #ccc", borderRadius: 8, padding: "8px 14px", cursor: "pointer" }}>
                Cancel
              </button>
              <button onClick={saveCurrentRoute} disabled={savingRoute} style={{ background: "#8e44ad", color: "#fff", border: "none", borderRadius: 8, padding: "8px 14px", cursor: "pointer" }}>
                {savingRoute ? "Saving..." : "Save"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ====== CHAT: launcher button & panel ====== */}
      <button
        onClick={() => setChatOpen(true)}
        style={{ position: "absolute", right: 20, bottom: 20, zIndex: 2100, background: "#8e44ad", color: "#fff", border: "none", borderRadius: 24, padding: "10px 16px", boxShadow: "0 8px 20px rgba(0,0,0,0.15)", cursor: "pointer" }}
        title="Open chat"
      >
        💬 Ask Here!
      </button>

      <ChatPanel open={chatOpen} onClose={() => setChatOpen(false)} width={LEFT_PANEL_WIDTH}>
        {/* Messages */}
        <div style={{ flex: 1, overflowY: "auto", padding: 12, background: "#fafafa" }}>
          {chatMsgs.map((m) => (
            <div key={m.id} style={{ display: "flex", justifyContent: m.role === "user" ? "flex-end" : "flex-start", marginBottom: 8 }}>
              <div
                style={{
                  maxWidth: "80%",
                  padding: "8px 12px",
                  borderRadius: 12,
                  background: m.role === "user" ? "#8e44ad" : "#ffffff",
                  color: m.role === "user" ? "#fff" : "#111",
                  border: m.role === "user" ? "none" : "1px solid #eee",
                  boxShadow: "0 1px 4px rgba(0,0,0,0.06)",
                }}
              >
                {m.text}
              </div>
            </div>
          ))}
        </div>

        {/* Composer */}
        <div style={{ borderTop: "1px solid #eee", padding: 10 }}>
          {showLocationCTA && (
            <div style={{ marginBottom: 8 }}>
              <button onClick={handleImHere} style={{ background: "#0ea5e9", color: "#fff", border: "none", borderRadius: 8, padding: "6px 10px", cursor: "pointer" }}>
                📍 I’m here
              </button>
            </div>
          )}

          <div style={{ display: "flex", gap: 8 }}>
            <input
              value={chatInput}
              onChange={(e) => setChatInput(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Enter") {
                  e.preventDefault();
                  sendToNlp(chatInput);
                }
              }}
              placeholder='Ask: “drive from Leicester to London via Oxford…”'
              style={{ flex: 1, border: "1px solid #ddd", borderRadius: 8, padding: "10px 12px", outline: "none" }}
            />
            <button onClick={() => sendToNlp(chatInput)} style={{ background: "#8e44ad", color: "#fff", border: "none", borderRadius: 8, padding: "10px 14px", cursor: "pointer" }}>
              Send
            </button>
          </div>
        </div>
      </ChatPanel>
      {/* ========================================= */}
    </div>
  );
}

export default MultiRouteMap;
