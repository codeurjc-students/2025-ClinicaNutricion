import UserForm from '../../components/UserForm';
import '../../styles/components/Form.css';

const CreateNutritionist = () => {
  return (
    <div className="content">
      <div className="Form">
        <h2>Registrar Nutricionista</h2>
        <UserForm userType="nutritionist" />
      </div>
    </div>
  );
};

export default CreateNutritionist;
