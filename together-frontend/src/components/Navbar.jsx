import {Link, useNavigate} from "react-router-dom";
import {useAuth} from "../providers/AuthProvider.jsx";
import {logout} from "../api/authService.js";

function Navbar({ isSidebarOpen, setIsSidebarOpen }) {
    const { isAuthenticated, user, setUser, setAuthStatus } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        setUser(null);
        setAuthStatus(false);
        navigate("/");
    };

    return (
        <nav className="bg-gray-800 text-white px-6 py-4 shadow-md fixed w-full z-40">
            <div className="max-w-7xl mx-auto flex justify-between items-center">
                <Link to="/" className="text-2xl font-bold text-white hover:text-blue-400 transition">
                    MyApp
                </Link>
                {!isAuthenticated ? (
                    <div className="space-x-4">
                        <Link to="/login" className="hover:text-blue-400 transition">
                            Login
                        </Link>
                        <Link to="/register" className="hover:text-blue-400 transition">
                            Register
                        </Link>
                    </div>
                    ):(
                        <div className="flex items-center space-x-4">
                            <div>{user.email}</div>
                            <button
                                onClick={handleLogout}
                                className="font-bold hover:text-blue-400 transition"
                            >
                                Logout
                            </button>
                            <button
                                onClick={() => setIsSidebarOpen(!isSidebarOpen)}
                                className="p-2 hover:bg-gray-700 rounded-lg transition"
                            >
                                {isSidebarOpen ? "✕" : "☰"}
                            </button>
                        </div>
                    )}
            </div>
        </nav>
    );
}

export default Navbar;
