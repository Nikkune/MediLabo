import Header from "./layouts/Header.tsx";
import {Route, Routes} from "react-router-dom";
import Home from "./pages/Home.tsx";
import Patients from "./pages/Patients.tsx";
import {Bounce, ToastContainer} from "react-toastify";
import Note from "./pages/Note.tsx";

export default function App() {
    return(
        <div>
            <Header/>
            <Routes>
                <Route path="/" element={<Home/>}/>
                <Route path="/patients" element={<Patients/>}/>
                <Route path="/notes" element={<Note/>}/>
            </Routes>
            <ToastContainer
                position="top-right"
                autoClose={5000}
                hideProgressBar={false}
                newestOnTop
                closeOnClick={false}
                rtl={false}
                pauseOnFocusLoss
                draggable
                pauseOnHover
                theme="colored"
                transition={Bounce}
            />
        </div>
    )
}