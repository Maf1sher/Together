import { Link } from "react-router-dom";

function NavBar() {
    return (
        <nav className="bg-gray-800 text-white px-6 py-4 shadow-md">
            <div className="max-w-7xl mx-auto flex justify-between items-center">
                <Link to="/" className="text-2xl font-bold text-white hover:text-blue-400 transition">
                    MyApp
                </Link>
                <div className="space-x-4">
                    <Link to="/login" className="hover:text-blue-400 transition">
                        Login
                    </Link>
                    <Link to="/register" className="hover:text-blue-400 transition">
                        Register
                    </Link>
                </div>
            </div>
        </nav>
    );
}

export default NavBar;
