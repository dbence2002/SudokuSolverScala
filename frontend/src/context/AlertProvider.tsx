import React from "react";
import { useState } from "react"

import AlertContext from "./AlertContext"

function AlertProvider({ children }) {
    const [alertData, setAlertData] = useState({
        title: "", message: "", isOpen: false
    });
    const alert = (title: string, message: string) => {
        setAlertData({title, message, isOpen: true});
    }
    const close = () => setAlertData(data => {
        return {...data, isOpen: false};
    });
    return (
        <AlertContext.Provider value={[alertData, alert, close]}>
            {children}
        </AlertContext.Provider>
    )
}

export default AlertProvider
