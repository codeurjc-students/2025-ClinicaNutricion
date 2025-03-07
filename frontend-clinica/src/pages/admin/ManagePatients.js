import { useNavigate } from "react-router-dom";
import BackButton from "../../components/BackButton";

const ManagePatients = () => {
    const navigate = useNavigate();

    return (
        <div>
            <BackButton />
            <h2 className="text-center">Gestión de Pacientes</h2>
            <div className="manage-users-buttons">
                <button className="btn btn-primary" onClick={() => navigate("/admin/manage-users/patients/create", 
                    { state: { prevTitle: "Gestión de Pacientes" } })}>Dar de alta
                </button>

                <button className="btn btn-primary" onClick={() => navigate("/admin/manage-users/patients/search", 
                    { state: { prevTitle: "Gestión de Pacientes" } })}>Buscar Paciente
                </button>
            </div>
        </div>
    );
};

export default ManagePatients;
