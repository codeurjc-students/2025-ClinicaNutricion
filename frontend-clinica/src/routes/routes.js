import React from 'react';
import { Route, Routes, Navigate } from 'react-router-dom';
import { useAuth } from 'react-oidc-context';
import Sidebar from '../components/Sidebar';
import UserForm from '../components/UserForm';
import BackButton from '../components/BackButton';
import SearchComponent from '../components/SearchComponent';
import ManagePatients from '../pages/management/ManagePatients';
import ManageNutritionists from '../pages/management/ManageNutritionists';
import ManageAuxiliaries from '../pages/management/ManageAuxiliaries';
import MainPatientScreen from '../pages/patient/MainPatientScreen';
import NutritionistSelection from '../pages/patient/NutritionistSelection';
import TimeSelection from '../pages/patient/TimeSelection';
import AppointmentConfirmation from '../pages/patient/AppointmentConfirmation';
import ManageUsers from '../pages/management/ManageUsers';
import ManagementAgendas from '../pages/management/ManagementAgendas';
import NutritionistAgenda from '../pages/nutritionist/NutritionistAgenda';
import PendingAppointments from '../pages/patient/PendingAppointments';
import Unauthorized from '../pages/Unauthorized';

const AppRoutes = () => {
  const auth = useAuth();

  // Redirige al login al no estar autenticado
  if (!auth.isAuthenticated) return <Navigate to="/" />;

  const roles = auth.user?.profile['cognito:groups'] || [];
  const getUserType = () => {
    if (roles.includes('admin')) return 'admin';
    if (roles.includes('nutritionist')) return 'nutritionists';
    if (roles.includes('auxiliary')) return 'auxiliaries';
    if (roles.includes('patient')) return 'patients';
    return null;
  };

  const userType = getUserType();

  // Reirige a una pagina de unauthorized si el usuario no tiene permisos
  const ProtectedRoute = ({ children, allowedRoles }) => {
    if (!roles.some((role) => allowedRoles.includes(role))) {
      return <Navigate to="/unauthorized" />;
    }
    return children;
  };

  return (
    <div className="main-container">
      <Sidebar />
      <Routes>
        <Route path="/unauthorized" element={<Unauthorized />} />
        <Route
          path="/:userType/profile"
          element={
            <ProtectedRoute
              allowedRoles={['admin', 'nutritionist', 'auxiliary', 'patient']}
            >
              {userType === 'patients' && (
                <BackButton defaultText="Menú principal" />
              )}
              <UserForm isEditMode={true} userType={userType} />
            </ProtectedRoute>
          }
        />

        <Route
          path="/nutritionists"
          element={<Navigate to="/nutritionist/agenda" replace />}
        />
        <Route
          path="/nutritionist/agenda"
          element={
            <ProtectedRoute allowedRoles={['nutritionist']}>
              <NutritionistAgenda />
            </ProtectedRoute>
          }
        />
        <Route
          path="/nutritionists/patients"
          element={
            <ProtectedRoute allowedRoles={['nutritionist']}>
              <ManagePatients userType="nutritionists" />
            </ProtectedRoute>
          }
        >
          <Route path="create" element={<UserForm userType="patients" />} />
          <Route
            path=":id"
            element={
              <>
                <BackButton defaultText="Buscar Pacientes" />
                <UserForm userType="patients" isEditMode={true} />
              </>
            }
          />
          <Route
            path="search"
            element={
              <SearchComponent entityType="patients" userType="nutritionists" />
            }
          />
        </Route>

        <Route
          path="/auxiliaries"
          element={<Navigate to="/auxiliaries/agenda" replace />}
        />
        <Route
          path="/auxiliaries/agenda"
          element={
            <ProtectedRoute allowedRoles={['auxiliary']}>
              <ManagementAgendas />
            </ProtectedRoute>
          }
        />
        <Route
          path="/auxiliaries/patients"
          element={
            <ProtectedRoute allowedRoles={['auxiliary']}>
              <ManagePatients userType="auxiliaries" />
            </ProtectedRoute>
          }
        >
          <Route path="create" element={<UserForm userType="patients" />} />
          <Route
            path=":id"
            element={
              <>
                <BackButton defaultText="Buscar Pacientes" />
                <UserForm userType="patients" isEditMode={true} />
              </>
            }
          />
          <Route
            path="search"
            element={
              <SearchComponent entityType="patients" userType="auxiliaries" />
            }
          />
        </Route>

        <Route
          path="/admin"
          element={<Navigate to="/admin/agenda" replace />}
        />
        <Route
          path="/admin/agenda"
          element={
            <ProtectedRoute allowedRoles={['admin']}>
              <ManagementAgendas />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/manage-users"
          element={
            <ProtectedRoute allowedRoles={['admin']}>
              <ManageUsers />
            </ProtectedRoute>
          }
        >
          <Route
            path="patients"
            element={<ManagePatients userType="admin" />}
          />
          <Route
            path="patients/create"
            element={
              <>
                <BackButton defaultText="Gestión de Pacientes" />
                <UserForm userType="patients" />
              </>
            }
          />
          <Route
            path="patients/:id"
            element={
              <>
                <BackButton defaultText="Buscar Pacientes" />
                <UserForm userType="patients" isEditMode={true} />
              </>
            }
          />
          <Route
            path="patients/search"
            element={
              <>
                <BackButton defaultText="Gestión de Pacientes" />
                <SearchComponent entityType="patients" userType="admin" />
              </>
            }
          />

          <Route path="nutritionists" element={<ManageNutritionists />} />
          <Route
            path="nutritionists/create"
            element={
              <>
                <BackButton defaultText="Gestión de Nutricionistas" />
                <UserForm userType="nutritionists" />
              </>
            }
          />
          <Route
            path="nutritionists/:id"
            element={
              <>
                <BackButton defaultText="Buscar Nutricionistas" />
                <UserForm userType="nutritionists" isEditMode={true} />
              </>
            }
          />
          <Route
            path="nutritionists/search"
            element={
              <>
                <BackButton defaultText="Gestión de Nutricionistas" />
                <SearchComponent entityType="nutritionists" userType="admin" />
              </>
            }
          />

          <Route path="auxiliaries" element={<ManageAuxiliaries />} />
          <Route
            path="auxiliaries/create"
            element={
              <>
                <BackButton defaultText="Gestión de Auxiliares" />
                <UserForm userType="auxiliaries" />
              </>
            }
          />
          <Route
            path="auxiliaries/:id"
            element={
              <>
                <BackButton defaultText="Buscar Auxiliares" />
                <UserForm userType="auxiliaries" isEditMode={true} />
              </>
            }
          />
          <Route
            path="auxiliaries/search"
            element={
              <>
                <BackButton defaultText="Gestión de Auxiliares" />
                <SearchComponent entityType="auxiliaries" userType="admin" />
              </>
            }
          />
        </Route>

        <Route
          path="/patients"
          element={
            <ProtectedRoute allowedRoles={['patient']}>
              <MainPatientScreen />
            </ProtectedRoute>
          }
        />
        <Route
          path="/patients/nutritionist-selection"
          element={
            <ProtectedRoute allowedRoles={['patient']}>
              <NutritionistSelection />
            </ProtectedRoute>
          }
        />
        <Route
          path="/patients/time-selection"
          element={
            <ProtectedRoute allowedRoles={['patient']}>
              <TimeSelection />
            </ProtectedRoute>
          }
        />
        <Route
          path="/patients/appointment-confirmation"
          element={
            <ProtectedRoute allowedRoles={['patient']}>
              <AppointmentConfirmation />
            </ProtectedRoute>
          }
        />
        <Route
          path="/patients/appointments/pending"
          element={
            <ProtectedRoute allowedRoles={['patient']}>
              <PendingAppointments />
            </ProtectedRoute>
          }
        />

        {/* Redirección por defecto */}
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </div>
  );
};

export default AppRoutes;
