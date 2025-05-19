import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import '../../styles/pages/NutritionistSelection.css';
import BackButton from '../../components/BackButton.js';

const NutritionistSelection = () => {
  const BASE_URL = process.env.REACT_APP_API_BASE_URL;
  const loc = useLocation();
  const { patient } = loc.state || {};
  const [selectedTime, setSelectedTime] = useState('a cualquier hora');
  const [filteredNutritionists, setFilteredNutritionists] = useState([]);
  const [selectedNutritionist, setSelectedNutritionist] = useState(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  // Al cambiar la franja horaria se obtienen los nutricionistas disponibles
  useEffect(() => {
    const fetchNutritionists = async () => {
      try {
        if (!selectedTime) {
          setFilteredNutritionists([]);
          return;
        }

        setLoading(true);
        const encodedTimeRange = encodeURIComponent(selectedTime);
        let url = `${BASE_URL}/nutritionists/filter?timeRange=${encodedTimeRange}`;

        const token = localStorage.getItem('token');
        const response = await fetch(url, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`,
          },
        });

        if (!response.ok) throw new Error('Error en la respuesta del servidor');
        const data = await response.json();
        setFilteredNutritionists(data); // Guarda lista filtrada
      } catch (error) {
        console.error('Error cargando nutricionistas:', error);
        setFilteredNutritionists([]); // En error, limpiar lista
      } finally {
        setLoading(false);
      }
    };

    fetchNutritionists();
  }, [selectedTime, BASE_URL]);

  // Actualiza la franja horaria seleccionada
  const handleTimeChange = (e) => {
    setSelectedTime(e.target.value);
  };

  // Marca el nutricionista elegido de entre los filtrados
  const handleNutritionistChange = (e) => {
    const selected = e.target.value;
    const nutritionist = filteredNutritionists.find((n) => n.name === selected);
    setSelectedNutritionist(nutritionist || null);
  };

  // Navega al selector de hora con el nutricionista y paciente
  const handleSelectButtonClick = () => {
    if (selectedNutritionist) {
      navigate('/patients/time-selection', {
        state: {
          patient,
          nutritionist: selectedNutritionist,
          timeRange: selectedTime,
        },
      });
    }
  };

  return (
    <div className="nutritionist-selection">
      <header>
        <BackButton defaultText="Selección de nutricionista" />
      </header>
      <div className="content-wrapper">
        <div className="select-time">
          <label htmlFor="time-range">Selecciona una franja horaria</label>
          <select
            id="time-range"
            value={selectedTime}
            onChange={handleTimeChange}
            className="form-control"
          >
            <option value="a cualquier hora">A cualquier hora</option>
            <option value="mañana">Por la mañana (9:00 - 12:00)</option>
            <option value="mediodía">A medio día (12:00 - 16:00)</option>
            <option value="tarde">Por la tarde (16:00 - 20:00)</option>
          </select>
        </div>

        {/* Selector de nutricionista filtrado por franja horaria */}
        <div className="select-nutritionist">
          <label htmlFor="nutritionist">Selecciona un nutricionista</label>
          <select
            id="nutritionist"
            className="form-control"
            onChange={handleNutritionistChange}
          >
            <option value="">Seleccionar nutricionista</option>
            {loading ? (
              <option>Cargando...</option>
            ) : filteredNutritionists.length > 0 ? (
              filteredNutritionists.map((nutritionist) => (
                <option key={nutritionist.idUser} value={nutritionist.name}>
                  {nutritionist.name}
                </option>
              ))
            ) : (
              <option>
                No hay nutricionistas disponibles para esta franja horaria
              </option>
            )}
          </select>
        </div>

        {/* Botón para confirmar selección */}
        <button
          disabled={!selectedNutritionist}
          className={`select-button ${selectedNutritionist ? 'active' : 'inactive'}`}
          onClick={handleSelectButtonClick}
        >
          Seleccionar
        </button>
      </div>
    </div>
  );
};

export default NutritionistSelection;
