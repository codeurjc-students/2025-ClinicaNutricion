import { useNavigate } from "react-router-dom";
import BackButton from "../../components/BackButton";

const ManageAuxiliaries = () => {
    const navigate = useNavigate();

    return (
        <div>
            <BackButton text="Gestión de Usuarios" to="/admin/manage-users" />
            <h2 className="text-center">Gestión de Auxiliares</h2>
            <div className="manage-users-buttons">
                <button className="btn btn-primary"onClick={() => navigate("/admin/manage-users/auxiliaries/create")}>Dar de alta</button>
                <button className="btn btn-primary" onClick={() => navigate("/admin/manage-users/auxiliaries/search")}>Buscar Auxiliar</button>
            </div>
        </div>
    );
};

export default ManageAuxiliaries;
