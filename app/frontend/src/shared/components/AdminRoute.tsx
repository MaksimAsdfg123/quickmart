import { Navigate } from 'react-router-dom'

import { useAuthStore } from '../lib/authStore'

export function AdminRoute({ children }: { children: JSX.Element }) {
  const user = useAuthStore((state) => state.user)
  if (!user) {
    return <Navigate to="/login" replace />
  }
  if (user.role !== 'ADMIN') {
    return <Navigate to="/" replace />
  }
  return children
}
