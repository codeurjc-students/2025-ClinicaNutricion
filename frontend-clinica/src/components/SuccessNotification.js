import React, { useEffect, useState } from "react";
import "../styles/components/SuccessNotification.css"; 
import successIcon from "../assets/icons/success-icon.png";

const SuccessNotification = ({ message, onClose }) => {
    const [visible, setVisible] = useState(true);

    useEffect(() => {
        const timer = setTimeout(() => {
            setVisible(false);
            onClose();
        }, 2500);

        return () => clearTimeout(timer);
    }, [onClose]);

    return (
        <div className={`success-notification ${!visible ? "fade-out" : ""}`}>
            <p>{message}</p>
            <img src={successIcon} alt="Éxito" className="success-icon" />
        </div>
    );
};

export default SuccessNotification;
