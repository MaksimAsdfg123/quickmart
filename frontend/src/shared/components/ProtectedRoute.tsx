import { Navigate, useLocation } from 'react-router-dom'

import { useAuthStore } from '../lib/authStore'

export function ProtectedRoute({ children }: { children: JSX.Element }) {
  const token = useAuthStore((state) => state.token)
  const location = useLocation()

  if (!token) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }

  return children
}
