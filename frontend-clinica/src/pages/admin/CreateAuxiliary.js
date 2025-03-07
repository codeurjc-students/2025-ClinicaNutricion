import UserForm from "../../components/UserForm";
import "../../styles/components/Form.css";

const CreateAuxiliary = () => {
    return (
        <div className="content">
            <div className="Form">
                <h2>Registrar Auxiliar</h2>
                <UserForm userType="auxiliary" />
            </div>
        </div>
    );
};

export default CreateAuxiliary;
