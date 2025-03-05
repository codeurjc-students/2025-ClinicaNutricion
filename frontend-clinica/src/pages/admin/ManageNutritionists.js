import BackButton from "../../components/BackButton";
import { useNavigate } from "react-router-dom";

const ManageNutritionists = () => {
    const navigate = useNavigate();


    return (           
        <div className="content">
            <BackButton text="Gestión de Usuarios" to="/admin/manage-users" />
            
            <h2>Gestión de Nutricionistas</h2>
            <div className="buttons-container">
                <button onClick={() => navigate("/admin/manage-users/nutritionists/create")}>Dar de alta</button>
                <button onClick={() => navigate("/admin/manage-users/nutritionists/search")}>Buscador</button>
            </div>    
        </div>
    );
};

export default ManageNutritionists;
