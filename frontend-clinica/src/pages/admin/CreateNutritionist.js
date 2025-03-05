import NutritionistForm from "../../components/NutritionistForm";
import "../../styles/global.css";
import "../../styles/Form.css";

const CreateNutritionist = () => {
    return (
        <div className="content">
            <div className="Form">
                <h2>Registrar Nutricionista</h2>
                <NutritionistForm isEditMode={false} />
            </div>
        </div>
    );
};

export default CreateNutritionist;
