import React, {useEffect, useState} from "react";

const arrowDirections = {
    "ArrowDown": [1, 0],
    "ArrowUp": [-1, 0],
    "ArrowLeft": [0, -1],
    "ArrowRight": [0, 1]
};

const SudokuCell = ({
    num,
    locked,
    disabled,
    onClick
}: {
    num: number;
    locked: boolean;
    disabled: boolean;
    onClick: () => void;
}) => {
    return (
        <button disabled={disabled} className={`w-full h-full enabled:hover:bg-gray-600 enabled:active:bg-gray-500 transition duration-200 text-lg sm:text-xl lg:text-2xl font-medium
            ${locked? "text-gray-200 bg-gray-600": "text-gray-500 bg-gray-700"} `} onClick={onClick}>
            {num === 0? undefined: num}
        </button>
    )
}
const SudokuTable = ({
    table,
    setTable,
    solution,
    disabled
}: {
    table: number[][];
    setTable: React.Dispatch<React.SetStateAction<number[][]>>;
    solution: number[][];
    disabled: boolean;
}) => {
    const changeAt = (i: number, j: number, v: number) => {
        setTable(table => {
            const newTable = [];
            for (let k = 0; k < 9; ++k) {
                newTable[k] = table[k].slice();
            }
            newTable[i][j] = v;
            return newTable;
        });
    }
    const advance = (i: number, j: number) => {
        changeAt(i, j, (table[i][j] + 1) % 10);
    }
    const [selected, setSelected] = useState([0, 0]);


    useEffect(() => {
        const handleKeyDown = (event) => {
            if (event.key in arrowDirections) {
                const dir = arrowDirections[event.key];
                setSelected(sel => {
                    const newSel = sel.slice();
                    newSel[0] = (newSel[0] + dir[0] + 9) % 9;
                    newSel[1] = (newSel[1] + dir[1] + 9) % 9;
                    return newSel;
                });
            }
            if (event.key >= '0' && event.key <= '9' && !disabled) {
                changeAt(selected[0], selected[1], parseInt(event.key));
            }
        };
        document.addEventListener('keydown', handleKeyDown);
        return () => {
            document.removeEventListener('keydown', handleKeyDown);
        };
    }, [selected, disabled]);

    return (
        <div className="aspect-square grid grid-cols-9 grid-rows-9 w-full">
            {table.map((row, i) => (
                row.map((num, j) => (
                    <div key={i * 9 + j} className={`border border-gray-900 w-full h-full relative
                            ${i !== 0 && i % 3 === 0? "border-t-4 sm:border-t-[5px]": ""}
                            ${j !== 0 && j % 3 === 0? "border-l-4 sm:border-l-[5px]": ""}`}>
                        <SudokuCell disabled={disabled} num={table[i][j] !== 0? num: solution[i][j]} onClick={() => {
                            setSelected(([x, y]) => {
                                if (x === i && y === j) {
                                    advance(i, j);
                                }
                                return [i, j];
                            });
                            setSelected([i, j]);
                        }} locked={table[i][j] !== 0}/>
                        <div className={`absolute inset-0 bg-lime-200 ${i === selected[0] && solution[0][0] === 0? "opacity-15": "opacity-0"} transition duration-100 ease-out pointer-events-none`} />
                        <div className={`absolute inset-0 bg-lime-200 ${j === selected[1] && solution[0][0] === 0? "opacity-15": "opacity-0"} transition duration-100 ease-out pointer-events-none`} />
                    </div>
                ))
            ))}
        </div>
    )
}

export default SudokuTable;