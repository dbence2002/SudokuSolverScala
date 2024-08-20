import React, {useContext, useEffect} from "react";
import {useState} from "react";
import SudokuTable from "./SudokuTable";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import Dropdown from "./Dropdown";
import AlertContext from "../context/AlertContext";

const initTable: number[][] = Array.from(Array(9), _ => Array(9).fill(0));
const algorithms: string[] = ["Backtracking", "Evolutionary"];

const SudokuSolver = () => {
    const fetchSolution = async (): Promise<number[][] | null> => {
        const resp = await fetch(`${process.env.REACT_APP_BACKEND_URL}/solve`, {
            method: "POST",
            body: JSON.stringify({
                table: table,
                algorithm: algorithms[chosen].toLowerCase()
            })
        })
        const json = await resp.json();
        return json.solution?.table;
    }
    const solve = async () => {
        setDisabled(true);
        setIsLoading(true);
        try {
            const solution = await fetchSolution();
            setIsLoading(false);
            if (solution) {
                setSolution(solution);
            } else {
                setDisabled(false);
                alert("No solution", "No solution found :(");
            }
        } catch (e) {
            alert("Error", "The fetching was unsuccessful");
            console.error(e);
            setDisabled(false);
            setIsLoading(false);
        }
    }
    const clearTable = () => {
        setTable(initTable);
        setSolution(initTable);
        setDisabled(false);
    }
    const resetSolution = () => {
        setSolution(initTable);
        setDisabled(false);
    }

    const [table, setTable] = useState(initTable);
    const [disabled, setDisabled] = useState(false);
    const [solution, setSolution] = useState(initTable);
    const [isLoading, setIsLoading] = useState(false);
    const [chosen, setChosen] = useState(0);
    const [alertData, alert] = useContext(AlertContext);

    useEffect(() => {
        if (alertData.isOpen) document.body.style.overflow = 'hidden';
        else document.body.style.overflow = 'unset';
    }, [alertData]);

    return (
        <>
            <div className="w-full flex flex-col items-center">
                <div className="w-full sm:w-[30rem] lg:w-[40rem]">
                    <Dropdown items={algorithms} chosen={chosen} setChosen={i => setChosen(i)} />
                    <SudokuTable table={table} setTable={setTable} solution={solution} disabled={disabled} />
                    <div className="flex justify-center space-x-[5px] sm:space-x-1.5 mt-1 sm:mt-1.5">
                        <button disabled={disabled} className="bg-indigo-700 disabled:text-indigo-400 relative enabled:hover:bg-indigo-600 px-6 py-3 font-medium rounded w-full enabled:active:bg-indigo-500 transition duration-200" onClick={solve}>
                            <span className={`${!isLoading? "opacity-100 ease-in": "opacity-0 ease-out"} absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 transition duration-200 space-x-2.5 flex items-center`}>
                                <FontAwesomeIcon icon={["fas", "play"]} className="w-4 h-4" />
                                <span>Solve</span>
                            </span>
                            <span className={`${isLoading? "opacity-100 ease-in": "opacity-0 ease-out"} absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 transition duration-200`}>
                                <svg aria-hidden="true" className="w-5 h-5 text-indigo-500 animate-spin fill-white" viewBox="0 0 100 101" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M100 50.5908C100 78.2051 77.6142 100.591 50 100.591C22.3858 100.591 0 78.2051 0 50.5908C0 22.9766 22.3858 0.59082 50 0.59082C77.6142 0.59082 100 22.9766 100 50.5908ZM9.08144 50.5908C9.08144 73.1895 27.4013 91.5094 50 91.5094C72.5987 91.5094 90.9186 73.1895 90.9186 50.5908C90.9186 27.9921 72.5987 9.67226 50 9.67226C27.4013 9.67226 9.08144 27.9921 9.08144 50.5908Z" fill="currentColor"/>
                                    <path d="M93.9676 39.0409C96.393 38.4038 97.8624 35.9116 97.0079 33.5539C95.2932 28.8227 92.871 24.3692 89.8167 20.348C85.8452 15.1192 80.8826 10.7238 75.2124 7.41289C69.5422 4.10194 63.2754 1.94025 56.7698 1.05124C51.7666 0.367541 46.6976 0.446843 41.7345 1.27873C39.2613 1.69328 37.813 4.19778 38.4501 6.62326C39.0873 9.04874 41.5694 10.4717 44.0505 10.1071C47.8511 9.54855 51.7191 9.52689 55.5402 10.0491C60.8642 10.7766 65.9928 12.5457 70.6331 15.2552C75.2735 17.9648 79.3347 21.5619 82.5849 25.841C84.9175 28.9121 86.7997 32.2913 88.1811 35.8758C89.083 38.2158 91.5421 39.6781 93.9676 39.0409Z" fill="currentFill"/>
                                </svg>
                            </span>
                        </button>
                        <button disabled={isLoading} className="bg-gray-700 disabled:text-gray-400 enabled:hover:bg-gray-600 px-6 py-3 font-medium rounded w-full enabled:active:bg-gray-500 transition duration-200 space-x-2.5" onClick={resetSolution}>
                            <FontAwesomeIcon icon={["fas", "refresh"]} className="w-4 h-4" />
                            <span>Reset</span>
                        </button>
                        <button disabled={isLoading} className="bg-red-700 disabled:text-red-400 enabled:hover:bg-red-600 px-6 py-3 font-medium rounded w-full enabled:active:bg-gray-500 transition duration-200 space-x-2.5" onClick={clearTable}>
                            <FontAwesomeIcon icon={["fas", "eraser"]} className="w-4 h-4" />
                            <span>Clear</span>
                        </button>
                    </div>
                </div>
            </div>
        </>
    )
}

export default SudokuSolver;