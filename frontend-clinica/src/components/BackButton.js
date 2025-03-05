import { useNavigate } from "react-router-dom";
import backIcon from "../assets/BackLogo.png";
import "../styles/global.css";

const BackButton = ({ text, to }) => {
    const navigate = useNavigate();

    return (
        <div className="back-button" onClick={() => navigate(to)}>
            <img src={backIcon} alt="Volver" className="back-icon" />
            <span>{text}</span>
        </div>
    );
};

export default BackButton;
