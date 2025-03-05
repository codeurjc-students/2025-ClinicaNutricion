import { useNavigate } from "react-router-dom";
import "../../styles/global.css";

const ManageUsers = () => {
    const navigate = useNavigate();

    return (
        <div className="content">
            <h1>Gestión de Usuarios</h1>
                <button className="global-button" onClick={() => navigate("/admin/manage-users/patients")}>Pacientes</button>
                <button className="global-button" onClick={() => navigate("/admin/manage-users/nutritionists")}>Nutricionistas</button>
                <button className="global-button" onClick={() => navigate("/admin/manage-users/auxiliaries")}>Auxiliares</button>
        </div>
    );
};

export default ManageUsers;
