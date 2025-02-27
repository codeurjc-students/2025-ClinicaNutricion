import { useNavigate } from "react-router-dom";

const ManagePatients = () => {
    const navigate = useNavigate();

    return (
        <div className="content">
            <h2>Gestión de Pacientes</h2>
            <div className="buttons-container">
                <button onClick={() => console.log("Registrar nuevo paciente")}>Dar de alta</button>
                <button onClick={() => console.log("Buscar paciente")}>Buscador</button>
            </div>
        </div>
    );
};

export default ManagePatients;
