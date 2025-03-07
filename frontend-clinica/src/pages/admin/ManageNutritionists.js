import BackButton from "../../components/BackButton";
import { useNavigate } from "react-router-dom";

const ManageNutritionists = () => {
    const navigate = useNavigate();


    return (           
        <div>
            <BackButton text="Gestión de Usuarios" to="/admin/manage-users" />
            <h2 className="text-center">Gestión de Nutricionistas</h2>
            <div className="manage-users-buttons">
                <button className="btn btn-primary" onClick={() => navigate("/admin/manage-users/nutritionists/create")}>Dar de alta</button>
                <button className="btn btn-primary" onClick={() => navigate("/admin/manage-users/nutritionists/search")}>Buscar Nutricionista</button>
            </div>    
        </div>
    );
};

export default ManageNutritionists;
