import React, { useState, useEffect } from 'react';
import { Container, Form, Spinner } from 'react-bootstrap';
import NutritionistCalendar from '../../components/NutritionistCalendar';
import '../../styles/pages/AdminAgenda.css';
import logoImg from '../../assets/sidebar/LogoClinicaPrincipal.png';

const AdminAgenda = () => {
  const BASE_URL = process.env.REACT_APP_API_BASE_URL;
  const [nutritionists, setNutritionists] = useState([]);
  const [selectedNutritionist, setSelectedNutritionist] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(false);
  const [showDropdown, setShowDropdown] = useState(false);
  /** 
  Cada vez que se cambia el valor en el buscador, se hace una nueva consulta
  para buscar nutricionistas que coincidan con el término de búsqueda
**/
  useEffect(() => {
    const fetchNutritionists = async () => {
      try {
        if (!searchTerm.trim()) {
          setNutritionists([]);
          setShowDropdown(false);
          return;
        }

        setLoading(true);
        let url = `${BASE_URL}/nutritionists`;

        if (searchTerm.trim() !== '') {
          url += `?fullName=${encodeURIComponent(searchTerm)}`;
        }

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
        setNutritionists(data);
        setShowDropdown(true);
      } catch (error) {
        console.error('Error cargando nutricionistas:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchNutritionists();
  }, [searchTerm, BASE_URL]);

  // Maneja cambios en el input del buscador
  const handleSearchChange = (e) => {
    const value = e.target.value;
    if (selectedNutritionist) {
      setSelectedNutritionist(null); // Si ya había uno seleccionado se limpia la selección al editar
      setSearchTerm('');
    } else {
      setSearchTerm(value);
    }
  };

  return (
    <Container className="admin-agenda-container">
      {/* Logo de la clínica mientras no se haya seleccionado un nutricionista */}
      {!selectedNutritionist && (
        <div className="admin-agenda-logo-bg">
          <img src={logoImg} alt="Logo Clínica" />
        </div>
      )}

      {/* Título dinámico según el nutricionista */}
      <h2 className="text-center">
        {selectedNutritionist
          ? `Agenda de ${selectedNutritionist.name} ${selectedNutritionist.surname}`
          : 'Agenda de Nutricionistas'}
      </h2>

      {/* Buscador de nutricionistas */}
      <Form.Group className="search-box">
        <Form.Control
          type="text"
          placeholder="Buscar nutricionista..."
          value={
            selectedNutritionist
              ? `${selectedNutritionist.name} ${selectedNutritionist.surname}`
              : searchTerm
          }
          onChange={handleSearchChange}
          className="search-input"
          onFocus={() => !selectedNutritionist && setShowDropdown(true)}
          onBlur={() => setTimeout(() => setShowDropdown(false), 200)}
        />
        {loading && (
          // Spinner mientras se espera a una respuesta
          <Spinner animation="border" variant="success" className="spinner" />
        )}
        {showDropdown && searchTerm.trim() && nutritionists.length > 0 && (
          // Lista de sugerencias (máximo 6)
          <div className="dropdown-suggestions">
            {nutritionists.slice(0, 6).map((nutri) => (
              <div
                key={nutri.idUser}
                className="suggestion-item"
                onClick={() => setSelectedNutritionist(nutri)}
              >
                {nutri.name} {nutri.surname}
              </div>
            ))}
            {nutritionists.length > 6 && (
              <div className="suggestion-item more-results">...</div>
            )}
          </div>
        )}
      </Form.Group>

      {/* Al seleccionar el nutricionista se muestra el calendario con sus citas */}
      {selectedNutritionist && (
        <NutritionistCalendar
          nutritionistId={selectedNutritionist.idUser}
          appointmentDuration={selectedNutritionist.appointmentDuration}
          startTime={selectedNutritionist.startTime}
          endTime={selectedNutritionist.endTime}
        />
      )}
    </Container>
  );
};

export default AdminAgenda;
