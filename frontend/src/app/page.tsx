export default function Home() {
  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-gray-50">
      <h1 className="text-4xl font-bold text-gray-900 mb-4">Welcome to Meal Planner AI</h1>
      <p className="text-lg text-gray-600 mb-8">Plan your meals with the power of AI</p>
      <div className="space-x-4">
        <a
          href="/login"
          className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
        >
          Login
        </a>
        <a
          href="/register"
          className="bg-green-500 hover:bg-green-700 text-white font-bold py-2 px-4 rounded"
        >
          Register
        </a>
      </div>
    </div>
  );
}
