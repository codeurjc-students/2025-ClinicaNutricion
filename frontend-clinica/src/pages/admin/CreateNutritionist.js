import NutritionistForm from "../../components/NutritionistForm";
import "../../styles/global.css";
import "../../styles/global.css";

const CreateNutritionist = () => {
    return (
        <div className="content">
            <h2>Registrar Nutricionista</h2>
            <NutritionistForm isEditMode={false} />
        </div>
    );
};

export default CreateNutritionist;
