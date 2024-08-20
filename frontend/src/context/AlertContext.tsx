import { createContext } from "react"

type AlertData = {
    title: string;
    message: string;
    isOpen: boolean;
};
type AlertContextData = [AlertData, (t: string, m: string) => void, () => void];

const AlertContext = createContext<AlertContextData>([null, () => null, () => null]);
export default AlertContext;
