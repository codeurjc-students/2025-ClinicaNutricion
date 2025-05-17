import { Link, Outlet, useLocation } from 'react-router-dom';
import '../../styles/pages/ManageUsers.css';

const ManageUsers = () => {
  const location = useLocation();
  const isBasePath = location.pathname === '/admin/manage-users';

  return (
    <div className="manage-users-container">
      {isBasePath && <h2 className="text-center">Gestión de Usuarios</h2>}

      {isBasePath && (
        <div className="manage-users-buttons">
          <Link to="patients" state={{ prevTitle: 'Gestión de Usuarios' }}>
            <button className="btn btn-primary">Pacientes</button>
          </Link>

          <Link to="nutritionists" state={{ prevTitle: 'Gestión de Usuarios' }}>
            <button className="btn btn-primary">Nutricionistas</button>
          </Link>
          <Link to="auxiliaries" state={{ prevTitle: 'Gestión de Usuarios' }}>
            <button className="btn btn-primary">Auxiliares</button>
          </Link>
        </div>
      )}

      <Outlet />
    </div>
  );
};

export default ManageUsers;
