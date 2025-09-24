import Header from "./layouts/Header.tsx";
import {Route, Routes} from "react-router-dom";
import Home from "./pages/Home.tsx";
import Patients from "./pages/Patients.tsx";

export default function App() {
    return(
        <div>
            <Header/>
            <Routes>
                <Route path="/" element={<Home/>}/>
                <Route path="/patients" element={<Patients/>}/>
            </Routes>
        </div>
    )
}