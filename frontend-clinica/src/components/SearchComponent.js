import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Modal, Button } from 'react-bootstrap';
import '../styles/components/SearchComponent.css';
import editIcon from '../assets/icons/edit-icon.png';
import deleteIcon from '../assets/icons/delete-icon.png';
import ToggleSwitch from './ToggleSwitch';
import { toast } from 'react-toastify';

const formatDate = (dateString) => {
  if (!dateString) return 'N/A';
  const date = new Date(dateString);
  return new Intl.DateTimeFormat('es-ES', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  }).format(date);
};

const formatGender = (gender) => {
  if (!gender) return 'N/A';
  return gender.charAt(0).toUpperCase() + gender.slice(1).toLowerCase();
};

const ITEMS_PER_PAGE = 10;

const SearchComponent = ({
  entityType,
  userType,
  onSelect,
  selectedPatient,
  showSelectButton = false,
  onlyActivePatients = false,
}) => {
  const BASE_URL = process.env.REACT_APP_API_BASE_URL;
  const [filters, setFilters] = useState({
    name: '',
    surname: '',
    phone: '',
    email: '',
    active: onlyActivePatients ? 'true' : '',
  });
  const [results, setResults] = useState([]);
  const [searched, setSearched] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [auxToDelete, setAuxToDelete] = useState(null);
  const navigate = useNavigate();

  const handleFilterChange = (e) => {
    setFilters({ ...filters, [e.target.name]: e.target.value });
  };

  const handleClearFilters = () => {
    setFilters({
      name: '',
      surname: '',
      phone: '',
      email: '',
      active: onlyActivePatients ? 'true' : '',
    });
    setResults([]);
    setErrorMessage('');
    setSearched(false);
    setCurrentPage(1);
  };

  const parseActiveFilter = (value) => {
    if (value === 'true') return 'true';
    if (value === 'false') return 'false';
    return '';
  };

  const handleSearch = async () => {
    try {
      const filtersWithoutActive = onlyActivePatients
        ? { ...filters, active: 'true' }
        : filters;
      const isOnlyActiveApplied =
        onlyActivePatients &&
        filters.active === 'true' &&
        !Object.values(filtersWithoutActive).some(
          (value) => value.trim() !== '' && value !== 'true',
        );

      if (isOnlyActiveApplied) {
        setErrorMessage('Debes ingresar al menos un filtro para buscar.');
        setTimeout(() => setErrorMessage(''), 3000);
        return;
      }

      const hasFilters = Object.values(filtersWithoutActive).some(
        (value) => value.trim() !== '',
      );
      if (!hasFilters) {
        setErrorMessage('Debes ingresar al menos un filtro para buscar.');
        setTimeout(() => setErrorMessage(''), 3000);
        return;
      }

      setSearched(true);

      const token = localStorage.getItem('token');
      if (!token) throw new Error('No se encontró un token de autenticación.');

      const queryParams = new URLSearchParams({
        ...filtersWithoutActive,
        active: parseActiveFilter(filtersWithoutActive.active),
      }).toString();

      const response = await fetch(`${BASE_URL}/${entityType}?${queryParams}`, {
        method: 'GET',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok)
        throw new Error(
          `Error en la búsqueda: ${response.status} ${response.statusText}`,
        );

      const data = await response.json();
      setResults(data);
      setCurrentPage(1);
    } catch (error) {
      console.error('Error buscando:', error);
    }
  };

  const handleToggleUserStatus = async (idUser, isActive) => {
    try {
      const token = localStorage.getItem('token');
      if (!token) throw new Error('No se encontró un token de autenticación.');

      const endpoint = `${BASE_URL}/${entityType}/${idUser}/status`;
      const response = await fetch(endpoint, {
        method: 'PUT',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(!isActive),
      });

      if (!response.ok)
        throw new Error('Error al cambiar el estado del usuario.');

      setResults(
        results.map((user) =>
          user.idUser === idUser ? { ...user, active: !isActive } : user,
        ),
      );
    } catch (error) {
      console.error('Error cambiando estado del usuario:', error);
      toast.error('No se pudo cambiar el estado del usuario.');
    }
  };

  const handleDeleteUser = async (idUser) => {
    try {
      const token = localStorage.getItem('token');
      if (!token) throw new Error('No se encontró un token de autenticación.');

      const response = await fetch(`${BASE_URL}/${entityType}/${idUser}`, {
        method: 'DELETE',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) throw new Error('Error al eliminar el usuario.');

      setResults(results.filter((user) => user.idUser !== idUser));
      toast.success('Usuario eliminado correctamente.');
    } catch (error) {
      console.error(error);
      toast.error('No se pudo eliminar el usuario.');
    }
  };

  //Modal borrar usuario
  const openDeleteModal = (idUser) => {
    setAuxToDelete(idUser);
    setShowDeleteModal(true);
  };

  const closeDeleteModal = () => {
    setShowDeleteModal(false);
    setAuxToDelete(null);
  };

  const confirmDelete = async () => {
    await handleDeleteUser(auxToDelete);
    closeDeleteModal();
  };

  const startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
  const endIndex = startIndex + ITEMS_PER_PAGE;
  const paginatedResults = results.slice(startIndex, endIndex);

  return (
    <div className="content">
      <div className="search-container">
        <h2 className="text-center">
          Buscar{' '}
          {entityType === 'nutritionists'
            ? 'Nutricionistas'
            : entityType === 'patients'
              ? 'Pacientes'
              : 'Auxiliares'}
        </h2>

        {/* Mensaje de error */}
        {errorMessage && (
          <p className="error-message text-center">{errorMessage}</p>
        )}

        {/* Formulario de búsqueda */}
        <div className="d-flex justify-content-center">
          <div className="search-form text-center">
            <input
              type="text"
              name="name"
              placeholder="Nombre"
              value={filters.name}
              onChange={handleFilterChange}
            />
            <input
              type="text"
              name="surname"
              placeholder="Apellidos"
              value={filters.surname}
              onChange={handleFilterChange}
            />
            <input
              type="text"
              name="phone"
              placeholder="Teléfono"
              value={filters.phone}
              onChange={handleFilterChange}
            />
            <input
              type="text"
              name="email"
              placeholder="Email"
              value={filters.email}
              onChange={handleFilterChange}
            />
            {entityType !== 'auxiliaries' && !onlyActivePatients && (
              <select
                name="active"
                value={filters.active}
                onChange={handleFilterChange}
              >
                <option value="">Todos</option>
                <option value="true">Activos</option>
                <option value="false">Inactivos</option>
              </select>
            )}

            <button onClick={handleSearch} className="search-btn">
              Buscar
            </button>
            <button onClick={handleClearFilters} className="clear-btn">
              Limpiar
            </button>
          </div>
        </div>

        {/* Mostrar paciente seleccionado */}
        {selectedPatient && (
          <div className="selected-patient mt-3 text-center">
            <p>
              <strong>Paciente seleccionado:</strong> {selectedPatient.name}{' '}
              {selectedPatient.surname}
              <button
                className="clear-selection-btn"
                onClick={() => onSelect(null)}
              >
                <span className="icon">&#10006;</span>
              </button>
            </p>
          </div>
        )}

        {/* Resultados de búsqueda */}
        <div className="table-responsive mt-4">
          <table className="results-table table table-bordered table-hover text-center">
            <thead>
              <tr>
                <th>Nombre</th>
                <th>Apellidos</th>
                {entityType === 'nutritionists' ? (
                  <>
                    <th>Duración de citas</th>
                    <th>Entrada</th>
                    <th>Salida</th>
                  </>
                ) : (
                  <th>Fecha de nacimiento</th>
                )}
                <th>Email</th>
                <th>Teléfono</th>
                <th>Género</th>

                {showSelectButton ? (
                  <th>Seleccionar</th>
                ) : (
                  <>
                    <th>Editar</th>
                    {entityType === 'auxiliaries' && <th>Eliminar</th>}
                    {(entityType === 'nutritionists' ||
                      entityType === 'patients') && (
                      <>
                        <th>Estado</th>
                        <th>Eliminar</th>
                      </>
                    )}
                  </>
                )}
              </tr>
            </thead>
            <tbody>
              {paginatedResults.length > 0 ? (
                paginatedResults.map((item) => (
                  <tr key={item.idUser}>
                    <td>{item.name}</td>
                    <td>{item.surname}</td>
                    {entityType === 'nutritionists' ? (
                      <>
                        <td>
                          {item.appointmentDuration
                            ? `${item.appointmentDuration} minutos`
                            : 'N/A'}
                        </td>
                        <td>
                          {item.startTime ? item.startTime.slice(0, 5) : 'N/A'}
                        </td>
                        <td>
                          {item.endTime ? item.endTime.slice(0, 5) : 'N/A'}
                        </td>
                      </>
                    ) : (
                      <td>{formatDate(item.birthDate)}</td>
                    )}
                    <td>{item.mail}</td>
                    <td>{item.phone}</td>
                    <td>{formatGender(item.gender)}</td>
                    {showSelectButton ? (
                      <td colSpan={2} style={{ textAlign: 'center' }}>
                        <button
                          className="select-btn"
                          onClick={() => onSelect(item)}
                        >
                          Seleccionar
                        </button>
                      </td>
                    ) : (
                      <>
                        <td className="action-cell">
                          <button
                            className="action-btn"
                            onClick={() =>
                              navigate(
                                userType === 'admin'
                                  ? `/${userType}/manage-users/${entityType}/${item.idUser}`
                                  : `/${userType}/${entityType}/${item.idUser}`,
                              )
                            }
                          >
                            <img
                              src={editIcon}
                              alt="Editar"
                              className="action-icon"
                            />
                          </button>
                        </td>
                        {(entityType === 'nutritionists' ||
                          entityType === 'patients') && (
                          <td>
                            <div className="toggle-container">
                              <ToggleSwitch
                                isActive={item.active}
                                onToggle={() =>
                                  handleToggleUserStatus(
                                    item.idUser,
                                    item.active,
                                  )
                                }
                              />
                            </div>
                          </td>
                        )}
                        <td className="action-cell">
                          <button
                            className="action-btn"
                            onClick={() => openDeleteModal(item.idUser)}
                          >
                            <img
                              src={deleteIcon}
                              alt="Eliminar"
                              className="action-icon"
                            />
                          </button>
                        </td>
                      </>
                    )}
                  </tr>
                ))
              ) : searched ? (
                <tr>
                  <td colSpan="10">No hay resultados</td>
                </tr>
              ) : (
                <tr className="empty-row">
                  <td colSpan="10">&nbsp;</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
        {results.length > ITEMS_PER_PAGE && (
          <div className="pagination">
            <button
              className="pagination-btn"
              onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 1))}
              disabled={currentPage === 1}
            >
              {'<'}
            </button>
            <span>
              Página {currentPage} de{' '}
              {Math.ceil(results.length / ITEMS_PER_PAGE)}
            </span>
            <button
              className="pagination-btn"
              onClick={() =>
                setCurrentPage((prev) =>
                  Math.min(
                    prev + 1,
                    Math.ceil(results.length / ITEMS_PER_PAGE),
                  ),
                )
              }
              disabled={
                currentPage === Math.ceil(results.length / ITEMS_PER_PAGE)
              }
            >
              {'>'}
            </button>
          </div>
        )}
      </div>
      {/*Modal de confirmación*/}
      <Modal
        show={showDeleteModal}
        onHide={() => setShowDeleteModal(false)}
        centered
      >
        <Modal.Header closeButton>
          <Modal.Title>Confirmar eliminación</Modal.Title>
        </Modal.Header>

        <Modal.Body>
          {entityType === 'nutritionists'
            ? '¿Estás seguro de que quieres eliminar este nutricionista? Se borrarán todas las citas asociadas.'
            : '¿Estás seguro de que quieres eliminar este auxiliar?'}
        </Modal.Body>

        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowDeleteModal(false)}>
            Cancelar
          </Button>
          <Button variant="danger" onClick={confirmDelete}>
            Eliminar
          </Button>
        </Modal.Footer>
      </Modal>
    </div>
  );
};

export default SearchComponent;
