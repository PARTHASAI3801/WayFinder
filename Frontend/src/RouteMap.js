import React, { useState } from 'react';
import { MapContainer, TileLayer, Polyline, useMap } from 'react-leaflet';
import polyline from '@mapbox/polyline';
import 'leaflet/dist/leaflet.css';
import './App.css';

const FitBounds = ({ coords }) => {
  const map = useMap();
  React.useEffect(() => {
    if (coords.length > 0) {
      map.fitBounds(coords);
    }
  }, [coords, map]);
  return null;
};

const RouteSearchMap = () => {
  const [start, setStart] = useState('');
  const [end, setEnd] = useState('');
  const [routeCoords, setRouteCoords] = useState([]);

  const handleSearch = async () => {
    if (!start || !end) {
      alert('Please enter both start and end locations.');
      return;
    }

    try {
      const res = await fetch(`http://localhost:8080/locations/getRoutePath?source=${encodeURIComponent(start)}&destination=${encodeURIComponent(end)}`);
      const data = await res.json();

      if (!data.geometry) {
        alert('No geometry found in response.');
        return;
      }

      const decoded = polyline.decode(data.geometry);
      const coords = decoded.map(([lat, lng]) => [lat, lng]);
      setRouteCoords(coords);
    } catch (error) {
      console.error('Error fetching route:', error);
      alert('Failed to fetch route. Check server or console.');
    }
  };

  return (
    <div className="route-container">
      <div className="input-section">
        <input
          type="text"
          placeholder="Start Location"
          value={start}
          onChange={(e) => setStart(e.target.value)}
        />
        <input
          type="text"
          placeholder="End Location"
          value={end}
          onChange={(e) => setEnd(e.target.value)}
        />
        <button onClick={handleSearch}>Find Route</button>
      </div>

      <MapContainer
        center={[52.6369, -1.1398]} // Leicester default
        zoom={7}
        scrollWheelZoom={true}
        className="map"
      >
        <TileLayer
          attribution='&copy; OpenStreetMap'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        {routeCoords.length > 0 && (
          <>
            <Polyline positions={routeCoords} color="blue" />
            <FitBounds coords={routeCoords} />
          </>
        )}
      </MapContainer>
    </div>
  );
};

export default RouteSearchMap;
