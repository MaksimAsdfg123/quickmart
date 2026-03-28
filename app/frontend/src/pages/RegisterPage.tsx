import { useMutation } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'

import { authApi, type RegisterPayload } from '../api/authApi'
import { useToast } from '../shared/components/ToastProvider'
import { Button } from '../shared/components/ui/Button'
import { FormShell } from '../shared/components/ui/FormShell'
import { Input } from '../shared/components/ui/Input'
import { useAuthStore } from '../shared/lib/authStore'
import { extractErrorMessage } from '../shared/lib/errors'

export function RegisterPage() {
  const navigate = useNavigate()
  const setAuth = useAuthStore((state) => state.setAuth)
  const { showToast } = useToast()

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterPayload>()

  const mutation = useMutation({
    mutationFn: authApi.register,
    onSuccess: (data) => {
      setAuth(data.token, data.user)
      showToast({ type: 'success', title: 'Аккаунт создан' })
      navigate('/')
    },
    onError: (error) => {
      showToast({ type: 'error', title: 'Ошибка регистрации', description: extractErrorMessage(error) })
    },
  })

  return (
    <div className="page page--form" data-testid="register-page">
      <FormShell
        title="Регистрация"
        subtitle="Создайте аккаунт, чтобы оформлять заказы в пару кликов."
        width="sm"
        variant="page"
        footer={
          <p className="muted">
            Уже есть аккаунт? <Link to="/login" data-testid="register-go-login">Войти</Link>
          </p>
        }
      >
        <form onSubmit={handleSubmit((values) => mutation.mutate(values))} data-testid="register-form">
          <div className="form-row">
            <label htmlFor="fullName">Имя и фамилия</label>
            <Input
              id="fullName"
              data-testid="register-full-name"
              hasError={Boolean(errors.fullName)}
              placeholder="Иван Петров"
              {...register('fullName', {
                required: 'Укажите имя',
                minLength: { value: 2, message: 'Минимум 2 символа' },
              })}
            />
            {errors.fullName ? <div className="error-text">{errors.fullName.message}</div> : null}
          </div>

          <div className="form-row">
            <label htmlFor="email">Email</label>
            <Input
              id="email"
              data-testid="register-email"
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
              data-testid="register-password"
              hasError={Boolean(errors.password)}
              placeholder="Минимум 8 символов"
              type="password"
              {...register('password', {
                required: 'Введите пароль',
                minLength: { value: 8, message: 'Минимум 8 символов' },
              })}
            />
            {errors.password ? <div className="error-text">{errors.password.message}</div> : null}
          </div>

          {mutation.isError ? <div className="error" data-testid="register-error">{extractErrorMessage(mutation.error)}</div> : null}

          <Button type="submit" size="lg" block disabled={mutation.isPending} data-testid="register-submit">
            Создать аккаунт
          </Button>
        </form>
      </FormShell>
    </div>
  )
}

