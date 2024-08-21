import React from "react";

const ButtonChooser = ({
    items,
    chosen,
    setChosen
}: {
    items: string[];
    chosen: number;
    setChosen: (i: number) => void;
}) => {
    return (
        <div className="flex space-x-1 text-[0.81rem] mb-1.5">
            {items.map((item, i) =>
                <button key={i} className={`w-full px-2 py-1 ${chosen === i? "bg-orange-700 hover:bg-orange-600": "bg-gray-700 hover:bg-gray-600"} rounded transition duration-200`} onClick={() => setChosen(i)}>
                    {item}
                </button>
            )}
        </div>
    )
}

export default ButtonChooser;