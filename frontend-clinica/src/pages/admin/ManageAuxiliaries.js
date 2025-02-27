import { useNavigate } from "react-router-dom";

const ManageAuxiliaries = () => {
    const navigate = useNavigate();

    return (
        <div className="content">
            <h2>Gestión de Auxiliares</h2>
            <button onClick={() => console.log("Registrar nuevo auxiliar")}>Dar de alta</button>
            <button onClick={() => console.log("Buscar auxiliar")}>Buscador</button>
        </div>
    );
};

export default ManageAuxiliaries;
