import React, {useContext} from 'react';
import './App.css';
import SudokuSolver from "./components/SudokuSolver";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import { library } from '@fortawesome/fontawesome-svg-core'
import { fas } from '@fortawesome/free-solid-svg-icons'
import { fab } from '@fortawesome/free-brands-svg-icons'
import AlertContext from "./context/AlertContext";
import Alert from "./components/Alert";

library.add(fab, fas)

function App() {
    const [alertData, alert, closeAlert] = useContext(AlertContext);
    return (
        <>
            <Alert title={alertData.title} message={alertData.message} isOpen={alertData.isOpen} onClose={closeAlert} />
            <div className="flex flex-col items-center pt-1.5 sm:pt-10 px-1.5">
                <h1 className="text-3xl lg:text-4xl font-semibold my-4 space-x-4">
                    <span>Sudoku solver</span>
                    <a href="https://github.com">
                        <FontAwesomeIcon icon={["fab", "github"]} />
                    </a>
                </h1>
                <SudokuSolver />
            </div>
        </>
    );
}

export default App;
