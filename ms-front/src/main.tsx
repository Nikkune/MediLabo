import {StrictMode} from "react";
import {createRoot} from "react-dom/client";
import {BrowserRouter} from "react-router-dom";
import "./assets/index.scss";
import App from "./App.tsx";

const rootEl = document.getElementById("root");
if (!rootEl) throw new Error('Element #root introuvable');

createRoot(rootEl).render(
    <StrictMode>
        <BrowserRouter>
            <App/>
        </BrowserRouter>
    </StrictMode>
);