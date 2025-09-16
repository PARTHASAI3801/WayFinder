import React, { useState, useMemo } from "react";
import {
  MapContainer,
  TileLayer,
  Polyline,
  useMapEvents,
  Marker,
  Popup,
  CircleMarker,
} from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import axios from "axios";
import polyline from "@mapbox/polyline";

const defaultIcon = new L.Icon({
  iconUrl: "https://unpkg.com/leaflet@1.9.3/dist/images/marker-icon.png",
  shadowUrl: "https://unpkg.com/leaflet@1.9.3/dist/images/marker-shadow.png",
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41],
});

const getPlaceName = async (lat, lng) => {
  try {
    const response = await fetch(
      `https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json`
    );
    const data = await response.json();
    return data.display_name || "Unknown location";
  } catch (error) {
    console.error("Reverse geocoding failed:", error);
    return "Unknown location";
  }
};

const MapClickHandler = ({ start, end, setStart, setMidpoints, setEnd, setPopupInfo }) => {
  useMapEvents({
    dblclick: async (e) => {
      const latlng = [e.latlng.lat, e.latlng.lng];
      const place = await getPlaceName(...latlng);
      setPopupInfo({ position: latlng, name: place });
      if (!start) setStart(latlng);
      else if (!end) setEnd(latlng);
    },
    contextmenu: async (e) => {
      const latlng = [e.latlng.lat, e.latlng.lng];
      if (start && !end) {
        setMidpoints((prev) => [...prev, latlng]);
        const place = await getPlaceName(...latlng);
        setPopupInfo({ position: latlng, name: place });
      }
    },
  });
  return null;
};

const ContributeRoute = () => {
  const [start, setStart] = useState(null);
  const [midpoints, setMidpoints] = useState([]);
  const [end, setEnd] = useState(null);
  const [description, setDescription] = useState("");
  const [transportMode, setTransportMode] = useState("");
  const [popupInfo, setPopupInfo] = useState(null);
  const [message, setMessage] = useState("");
  const [pathOption, setPathOption] = useState("straight");
  const [finalPath, setFinalPath] = useState([]);

  const coordinates = useMemo(() => {
    return start ? [start, ...midpoints, ...(end ? [end] : [])] : [];
  }, [start, midpoints, end]);

  const alignRoute = async () => {
    if (coordinates.length < 2 || !transportMode) {
      setMessage("Draw a route and select transport mode first.");
      setTimeout(() => {
        setMessage("");
      }, 5000);
      return;
    }

    const aligned = [];

    for (let i = 0; i < coordinates.length - 1; i++) {
      const from = `${coordinates[i][0]},${coordinates[i][1]}`;
      const to = `${coordinates[i + 1][0]},${coordinates[i + 1][1]}`;
      try {
        const res = await fetch(
          `http://localhost:8080/locations/getRoutePath?source=${from}&destination=${to}&mode=${transportMode}`
        );
        const segment = await res.json();
        const decoded = polyline.decode(segment.geometry);
        aligned.push(...decoded);
      } catch (err) {
        console.error("Failed aligning route:", err);
        setMessage("Failed to align route. Try again.");
        setTimeout(() => {
        setMessage("");
      }, 5000);
        return;
      }
    }

    setFinalPath(aligned);
    setMessage("Route aligned with roads.");
    setTimeout(() => {
        setMessage("");
      }, 5000);
  };

  const handlePathToggle = (type) => {
    setPathOption(type);
    if (type === "align") {
      alignRoute();
    } else {
      setFinalPath([]);
      setMessage("Using the drawn path.");
      setTimeout(() => {
        setMessage("");
      }, 5000);
    }
  };

  const handleSubmit = async () => {
    if (coordinates.length < 2 || !description) {
      setMessage("Draw a valid route and enter description.");
      setTimeout(() => {
        setMessage("");
      }, 5000);
      return;
    }

    const userId = localStorage.getItem("userId");
    if (!userId) {
      setMessage("User not logged in.");
      setTimeout(() => {
        setMessage("");
      }, 5000);
      return;
    }

    const startPlace = await getPlaceName(...start);
    const endPlace = await getPlaceName(...end);

    const coords = coordinates;

    const payload = {
      userId: parseInt(userId),
      routeType: "INTERNAL",
      startPlace,
      endplace: endPlace,
      description,
      transportMode,
      coordinates: coords,
    };

    try {
      await axios.post("http://localhost:8080/locations/contributeRoute", payload);
      setMessage("Route was submitted successfully!");
      setTimeout(() => {
        setMessage("");
      }, 5000);
      resetDrawing();
    } catch (err) {
      console.error(err);
      setMessage("Submission failed. Try again.");
      setTimeout(() => {
        setMessage("");
      }, 5000);
    }
  };

  const resetDrawing = () => {
    setStart(null);
    setMidpoints([]);
    setEnd(null);
    setDescription("");
    setPopupInfo(null);
    setTransportMode("");
    setPathOption("straight");
    setFinalPath([]);
  };

  return (
    <div style={{ display: "flex", height: "100vh" }}>
      <div style={{ width: "30%", padding: "20px", background: "#fff", boxShadow: "2px 0 10px rgba(0,0,0,0.1)", borderTopRightRadius: "30px", borderBottomRightRadius: "30px", fontFamily: "Segoe UI" }}>
        <h2 style={{ fontWeight: 600, fontSize: "24px", marginBottom: "16px" }}>🧭 Contribute your Route</h2>
        <textarea
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          placeholder="Describe this route..."
          rows={3}
          style={{ width: "100%", padding: "10px", marginBottom: "15px", borderRadius: "8px", border: "1px solid #ccc" }}
        />

        <div style={{ marginBottom: "15px" }}>
  <label style={{ fontWeight: 600 }}>Transport Mode:</label><br />
  {[
    "foot-walking",
    "cycling-regular",
    "driving-car",
    "driving-hgv",
  ].map((m) => (
    <button
      key={m}
      onClick={() => setTransportMode(m)}
      style={{
        margin: "5px 8px 12px 0",
        padding: "8px 10px",
        background: transportMode === m ? "#6A5ACD" : "#E0E0E0",
        color: transportMode === m ? "#fff" : "#333",
        border: "none",
        borderRadius: "6px",
        fontWeight: 500,
      }}
    >
      {m.replace("-", " ")}
    </button>
  ))}
</div>

<div style={{ marginBottom: "15px" }}>
  <label style={{ fontWeight: 600 }}>Route Option:</label><br />
  <button
    onClick={() => handlePathToggle("align")}
    style={{
      margin: "5px 10px 10px 0",
      padding: "8px 14px",
      backgroundColor: pathOption === "align" ? "#00A8E8" : "#ccc",
      color: "#fff",
      border: "none",
      borderRadius: "6px",
    }}
  >
    Align with Roads
  </button>
  <button
    onClick={() => handlePathToggle("straight")}
    style={{
      padding: "8px 14px",
      backgroundColor: pathOption === "straight" ? "#00A8E8" : "#ccc",
      color: "#fff",
      border: "none",
      borderRadius: "6px",
    }}
  >
    Keep as Drawn
  </button>
</div>


        <div style={{ marginTop: 20 }}>
          <button
            onClick={handleSubmit}
            style={{
              padding: "10px 20px",
              background: "#28a745",
              color: "#fff",
              border: "none",
              borderRadius: "6px",
              fontWeight: 600,
              marginRight: 10,
            }}
          >
            Submit
          </button>
          <button
            onClick={resetDrawing}
            style={{
              padding: "10px 20px",
              background: "#dc3545",
              color: "#fff",
              border: "none",
              borderRadius: "6px",
              fontWeight: 600,
            }}
          >
            Reset
          </button>
        </div>

        <p style={{ marginTop: "15px", color: message.includes("❌") ? "red" : "green" }}>{message}</p>
        <small style={{ color: "#666" }}>
          🖱️ Double-click to set Start & End<br />
          🖱️ Right-click to add via points
        </small>
      </div>

      <div style={{ width: "70%" }}>
        <MapContainer center={[52.6369, -1.1398]} zoom={13} style={{ height: "100%" }}>
          <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />

          <MapClickHandler
            start={start}
            end={end}
            setStart={setStart}
            setMidpoints={setMidpoints}
            setEnd={setEnd}
            setPopupInfo={setPopupInfo}
          />

          {start && <Marker position={start} icon={defaultIcon}><Popup> Start</Popup></Marker>}
          {midpoints.map((p, i) => (
            <CircleMarker key={i} center={p} radius={4} pathOptions={{ color: "blue" }} />
          ))}
          {end && <Marker position={end} icon={defaultIcon}><Popup> End</Popup></Marker>}

          {finalPath.length > 1 ? (
            <Polyline positions={finalPath} color="green" />
          ) : coordinates.length > 1 ? (
            <Polyline positions={coordinates} color="green" />
          ) : null}

          {popupInfo && <Popup position={popupInfo.position}>{popupInfo.name}</Popup>}
        </MapContainer>
      </div>
    </div>
  );
};

export default ContributeRoute;
