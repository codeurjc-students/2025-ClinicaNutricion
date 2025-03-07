import React, { useEffect, useState } from "react";
import { Alert } from "react-bootstrap";
import "../styles/components/SuccessNotification.css"; 
import successIcon from "../assets/success-icon.png";

const SuccessNotification = ({ message, onClose }) => {
    const [show, setShow] = useState(true);

    useEffect(() => {
        const timer = setTimeout(() => {
            setShow(false);
            setTimeout(onClose, 500); 
        }, 2000);

        return () => clearTimeout(timer);
    }, [onClose]);

    return (
        <Alert 
            show={show} 
            variant="success" 
            className={`success-notification ${show ? "fade-in" : "fade-out"}`}
        >
            <strong>{message}</strong>
            <img src={successIcon} alt="Success" className="success-icon" />
        </Alert>
    );
};

export default SuccessNotification;
