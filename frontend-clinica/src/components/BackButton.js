import { useNavigate, useLocation } from "react-router-dom";
import backIcon from "../assets/icons/back-icon.png";

const BackButton = ({ defaultText }) => {
    const navigate = useNavigate();
    const location = useLocation();

    const prevTitle = location.state?.prevTitle?.trim() || defaultText?.trim();

    if (!prevTitle) return null;

    return (
        <button className="back-button" onClick={() => navigate(-1)}>
            <img src={backIcon} alt="Volver" className="back-icon" />
            <span>{prevTitle}</span>
        </button>
    );
};

export default BackButton;
