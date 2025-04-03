import { Link } from "react-router-dom";

function LoginPage() {
    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-900">
            <div className="bg-gray-800 p-8 rounded-2xl shadow-lg w-96">
                <h2 className="text-white text-2xl font-semibold text-center mb-6">Log In</h2>
                <form>
                    <div className="mb-4">
                        <label className="block text-gray-400 text-sm mb-2">Email</label>
                        <input
                            type="email"
                            className="w-full p-3 bg-gray-700 text-white rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                            placeholder="Enter your email"
                        />
                    </div>
                    <div className="mb-4">
                        <label className="block text-gray-400 text-sm mb-2">Password</label>
                        <input
                            type="password"
                            className="w-full p-3 bg-gray-700 text-white rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                            placeholder="Enter your password"
                        />
                    </div>
                    <button
                        type="submit"
                        className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold p-3 rounded-lg transition duration-300">
                        Log In
                    </button>
                </form>
                <p className="text-gray-400 text-sm text-center mt-4">
                    Don't have an account?
                    <Link
                        to={"/register"}
                        className="text-blue-500 hover:underline"
                    >
                        Sign Up
                    </Link>
                </p>
            </div>
        </div>
    );
}

export default LoginPage;