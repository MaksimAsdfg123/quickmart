import { useMutation } from '@tanstack/react-query'
import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useLocation, useNavigate } from 'react-router-dom'

import { authApi, type LoginPayload } from '../api/authApi'
import { useToast } from '../shared/components/ToastProvider'
import { Button } from '../shared/components/ui/Button'
import { FormShell } from '../shared/components/ui/FormShell'
import { Input } from '../shared/components/ui/Input'
import { useAuthStore } from '../shared/lib/authStore'
import { extractErrorMessage } from '../shared/lib/errors'

export function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const setAuth = useAuthStore((state) => state.setAuth)
  const { showToast } = useToast()

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginPayload>({
    defaultValues: { email: 'anna@example.com', password: 'password' },
  })

  useEffect(() => {
    const searchParams = new URLSearchParams(location.search)
    if (searchParams.get('reason') === 'session-expired') {
      showToast({ type: 'info', title: 'Сессия истекла', description: 'Войдите снова, чтобы продолжить' })
    }
  }, [location.search, showToast])

  const mutation = useMutation({
    mutationFn: authApi.login,
    onSuccess: (data) => {
      setAuth(data.token, data.user)
      const searchParams = new URLSearchParams(location.search)
      const fallbackFrom = searchParams.get('from')
      const from = (location.state as { from?: string } | null)?.from ?? fallbackFrom ?? '/'
      showToast({ type: 'success', title: 'Вы вошли в аккаунт' })
      navigate(from)
    },
    onError: (error) => {
      showToast({ type: 'error', title: 'Ошибка входа', description: extractErrorMessage(error) })
    },
  })

  return (
    <div className="page page--form">
      <FormShell
        title="Вход"
        subtitle="Быстрый вход для оформления заказов и управления доставкой."
        width="sm"
        variant="page"
        footer={
          <p className="muted">
            Нет аккаунта? <Link to="/register">Зарегистрироваться</Link>
          </p>
        }
      >
        <div className="auth-meta">
          <strong>Демо-доступ</strong>
          <span className="muted muted--compact">admin@quickmart.local / password</span>
          <span className="muted muted--compact">anna@example.com / password</span>
        </div>

        <form onSubmit={handleSubmit((values) => mutation.mutate(values))}>
          <div className="form-row">
            <label htmlFor="email">Email</label>
            <Input
              id="email"
              hasError={Boolean(errors.email)}
              placeholder="you@example.com"
              type="email"
              {...register('email', {
                required: 'Введите email',
                pattern: { value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: 'Неверный формат email' },
              })}
            />
            {errors.email ? <div className="error-text">{errors.email.message}</div> : null}
          </div>

          <div className="form-row">
            <label htmlFor="password">Пароль</label>
            <Input
              id="password"
              hasError={Boolean(errors.password)}
              placeholder="Ваш пароль"
              type="password"
              {...register('password', { required: 'Введите пароль' })}
            />
            {errors.password ? <div className="error-text">{errors.password.message}</div> : null}
          </div>

          {mutation.isError ? <div className="error">{extractErrorMessage(mutation.error)}</div> : null}

          <Button type="submit" size="lg" block disabled={mutation.isPending}>
            Войти
          </Button>
        </form>
      </FormShell>
    </div>
  )
}
