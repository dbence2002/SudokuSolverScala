import {Dialog, Transition} from "@headlessui/react";
import React, {Fragment} from "react";

const Alert = ({
    title,
    message,
    isOpen,
    onClose
}: {
    title: String;
    message: string;
    isOpen: boolean;
    onClose: () => void;
}) => {
    return (
        <Transition
            show={isOpen}
            as={Fragment}
            enter="ease-in duration-300"
            enterFrom="opacity-0"
            enterTo="opacity-100"
            leave="ease-out duration-200"
            leaveFrom="opacity-100"
            leaveTo="opacity-0"
        >
            <Dialog static onClose={onClose} as="div">
                <Transition.Child
                    as={Fragment}
                    enter="ease-out duration-300"
                    enterFrom="opacity-0"
                    enterTo="opacity-100"
                    leave="ease-in duration-200"
                    leaveFrom="opacity-100"
                    leaveTo="opacity-0"
                >
                    <div className="fixed inset-0 bg-black/60" aria-hidden="true" />
                </Transition.Child>
                <Transition.Child
                    as={Fragment}
                    enter="ease-out duration-300"
                    enterFrom="opacity-0 translate-y-4 sm:translate-y-0 sm:scale-95"
                    enterTo="opacity-100 translate-y-0 sm:scale-100"
                    leave="ease-in duration-200"
                    leaveFrom="opacity-100 translate-y-0 sm:scale-100"
                    leaveTo="opacity-0 translate-y-4 sm:translate-y-0 sm:scale-95"
                >
                    <div className="fixed inset-0 flex items-center justify-center p-4">
                        <Dialog.Panel className="w-full sm:max-w-sm">
                            <Dialog.Title className="rounded-t-lg bg-gray-700 px-6 py-3 font-medium">
                                {title}
                            </Dialog.Title>
                            <div className="items-start px-6 py-3 bg-gray-800 text-sm">
                                {message}
                            </div>
                            <div className="px-6 border-t border-gray-700 bg-gray-800 rounded-b-lg py-3">
                                <button className="py-2 px-4 bg-indigo-600 rounded-lg hover:bg-indigo-700 active:bg-indigo-800 text-sm font-medium" onClick={onClose}>
                                    Close
                                </button>
                            </div>
                        </Dialog.Panel>
                    </div>
                </Transition.Child>
            </Dialog>
        </Transition>
    )
}

export default Alert;
