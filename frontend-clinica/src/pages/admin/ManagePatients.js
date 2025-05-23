import { useNavigate, Outlet, useLocation } from 'react-router-dom';
import BackButton from '../../components/BackButton';
import '../../styles/pages/ManageUsers.css';

const ManagePatients = ({ userType }) => {
  const navigate = useNavigate();
  const location = useLocation();

  // Construye ruta base según tipo de usuario
  const basePath =
    userType === 'admin'
      ? '/admin/manage-users/patients'
      : `/${userType}/patients`;

  // Determina si estamos en una subpágina para mostrar o no determinados elementos de la pagina
  const isSubPage =
    location.pathname.includes('/create') ||
    location.pathname.includes('/search') ||
    (!location.pathname.endsWith('/patients') &&
      location.pathname.includes('/manage-users/patients')) ||
    (!location.pathname.endsWith('/patients') &&
      location.pathname.includes('/patients'));

  return (
    <div className="manage-users-sub-container">
      <BackButton />
      {!isSubPage && (
        <>
          <h2 className="text-center">Gestión de Pacientes</h2>
          <div className="manage-users-buttons">
            <button
              className="btn btn-primary"
              onClick={() =>
                navigate(`${basePath}/create`, {
                  state: { prevTitle: 'Gestión de Pacientes' },
                })
              }
            >
              Dar de alta
            </button>

            <button
              className="btn btn-primary"
              onClick={() =>
                navigate(`${basePath}/search`, {
                  state: { prevTitle: 'Gestión de Pacientes' },
                })
              }
            >
              Buscar Paciente
            </button>
          </div>
        </>
      )}

      <Outlet />
    </div>
  );
};

export default ManagePatients;
