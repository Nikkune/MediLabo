import Header from "./layouts/Header.tsx";
import {Route, Routes} from "react-router-dom";
import Home from "./pages/Home.tsx";
import Patients from "./pages/Patients.tsx";
import {Bounce, ToastContainer} from "react-toastify";
import Notes from "./pages/Notes.tsx";
import {Box} from "@mui/material";

export default function App() {
    return (
        <Box sx={{width: '100%', height: '100%', display: 'flex', flexDirection: 'column'}}>
            <Header/>
            <Routes>
                <Route path="/" element={<Home/>}/>
                <Route path="/patients" element={<Patients/>}/>
                <Route path="/notes" element={<Notes/>}/>
            </Routes>
            <ToastContainer
                position="bottom-right"
                autoClose={3000}
                hideProgressBar={false}
                newestOnTop
                closeOnClick={true}
                rtl={false}
                pauseOnFocusLoss
                draggable
                pauseOnHover
                theme="colored"
                transition={Bounce}
            />
        </Box>
    )
}