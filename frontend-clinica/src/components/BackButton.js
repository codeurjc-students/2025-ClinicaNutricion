import { useNavigate, useLocation } from "react-router-dom";
import backIcon from "../assets/BackLogo.png";

const BackButton = ({ defaultText = "Volver" }) => {
    const navigate = useNavigate();
    const location = useLocation();
    
    const prevTitle = location.state?.prevTitle || defaultText;

    return (
        <button className="back-button" onClick={() => navigate(-1)}>
            <img src={backIcon} alt="Volver" className="back-icon" />
            <span>{prevTitle}</span>
        </button>
    );
};

export default BackButton;
