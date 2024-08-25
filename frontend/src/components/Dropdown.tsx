import {Menu, MenuButton, MenuItem, MenuItems} from "@headlessui/react";
import React from "react";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";

const Dropdown = ({
    items,
    chosen,
    setChosen
}: {
    items: string[];
    chosen: number;
    setChosen: (i: number) => void;
}) => {
    return (
        <div className="relative text-sm text-[0.92rem] font-medium">
            <Menu>
                {({ open }) => (
                    <>
                        <MenuButton className={`px-4 py-2.5 flex justify-between items-center bg-gray-950 rounded-md border border-gray-700 w-full mb hover:bg-gray-900 active:bg-gray-800 text-left ${open? "ring-2 ring-indigo-600": ""}`} as="button">
                            {items[chosen]}
                            <FontAwesomeIcon icon={["fas", "chevron-down"]} className={`w-3.5 h-3.5 ${open? "rotate-180": "rotate-0"} transition duration-150`} />
                        </MenuButton>
                        <MenuItems static className={` bg-gray-950 w-full absolute top-11 z-20 divide-y divide-gray-800 border border-gray-800 transition duration-150 ${open? "scale-100 opacity-100": "scale-95 opacity-0 pointer-events-none"}`} as="div">
                            {items.map((item, i) =>
                                <MenuItem key={i} className="px-4 py-2 hover:bg-gray-900 active:bg-gray-800 block w-full text-left" as="button" onClick={() => setChosen(i)}>
                                    {item}
                                </MenuItem>
                            )}
                        </MenuItems>
                    </>
                )}
            </Menu>
        </div>
    )
}

export default Dropdown;