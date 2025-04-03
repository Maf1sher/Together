import LoginPage from "./pages/LoginPage.jsx";
import {BrowserRouter as Router, Navigate, Route, Routes} from "react-router-dom";
import ScrollToTop from "./Layout/ScrollToTop.js";
import {NoLayout} from "./Layout/NoLayout.jsx";
import {Layout} from "./Layout/Layout.jsx";
import RegisterPage from "./pages/RegisterPage.jsx";

function AppRoutes() {
    return (
        <Router>
            <ScrollToTop />
            <Routes>

                <Route path="/" element={<Navigate to="/" />} />

                <Route element={<NoLayout/>}>
                    <Route path="/login" element={<LoginPage/>}/>
                    <Route path="/register" element={<RegisterPage/>}/>
                </Route>

                <Route element={<Layout/>}>

                </Route>
            </Routes>
        </Router>
    )
}

export default AppRoutes;