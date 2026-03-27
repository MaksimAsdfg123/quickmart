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
      showToast({ type: 'info', title: 'РЎРµСЃСЃРёСЏ РёСЃС‚РµРєР»Р°', description: 'Р’РѕР№РґРёС‚Рµ СЃРЅРѕРІР°, С‡С‚РѕР±С‹ РїСЂРѕРґРѕР»Р¶РёС‚СЊ' })
    }
  }, [location.search, showToast])

  const mutation = useMutation({
    mutationFn: authApi.login,
    onSuccess: (data) => {
      setAuth(data.token, data.user)
      const searchParams = new URLSearchParams(location.search)
      const fallbackFrom = searchParams.get('from')
      const from = (location.state as { from?: string } | null)?.from ?? fallbackFrom ?? '/'
      showToast({ type: 'success', title: 'Р’С‹ РІРѕС€Р»Рё РІ Р°РєРєР°СѓРЅС‚' })
      navigate(from)
    },
    onError: (error) => {
      showToast({ type: 'error', title: 'РћС€РёР±РєР° РІС…РѕРґР°', description: extractErrorMessage(error) })
    },
  })

  return (
    <div className="page page--form" data-testid="login-page">
      <FormShell
        title="Р’С…РѕРґ"
        subtitle="Р‘С‹СЃС‚СЂС‹Р№ РІС…РѕРґ РґР»СЏ РѕС„РѕСЂРјР»РµРЅРёСЏ Р·Р°РєР°Р·РѕРІ Рё СѓРїСЂР°РІР»РµРЅРёСЏ РґРѕСЃС‚Р°РІРєРѕР№."
        width="sm"
        variant="page"
        footer={
          <p className="muted">
            РќРµС‚ Р°РєРєР°СѓРЅС‚Р°? <Link to="/register" data-testid="login-go-register">Р—Р°СЂРµРіРёСЃС‚СЂРёСЂРѕРІР°С‚СЊСЃСЏ</Link>
          </p>
        }
      >
        <div className="auth-meta" data-testid="login-demo-credentials">
          <strong>Р”РµРјРѕ-РґРѕСЃС‚СѓРї</strong>
          <span className="muted muted--compact">admin@quickmart.local / password</span>
          <span className="muted muted--compact">anna@example.com / password</span>
        </div>

        <form onSubmit={handleSubmit((values) => mutation.mutate(values))} data-testid="login-form">
          <div className="form-row">
            <label htmlFor="email">Email</label>
            <Input
              id="email"
              data-testid="login-email"
              hasError={Boolean(errors.email)}
              placeholder="you@example.com"
              type="email"
              {...register('email', {
                required: 'Р’РІРµРґРёС‚Рµ email',
                pattern: { value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: 'РќРµРІРµСЂРЅС‹Р№ С„РѕСЂРјР°С‚ email' },
              })}
            />
            {errors.email ? <div className="error-text">{errors.email.message}</div> : null}
          </div>

          <div className="form-row">
            <label htmlFor="password">РџР°СЂРѕР»СЊ</label>
            <Input
              id="password"
              data-testid="login-password"
              hasError={Boolean(errors.password)}
              placeholder="Р’Р°С€ РїР°СЂРѕР»СЊ"
              type="password"
              {...register('password', { required: 'Р’РІРµРґРёС‚Рµ РїР°СЂРѕР»СЊ' })}
            />
            {errors.password ? <div className="error-text">{errors.password.message}</div> : null}
          </div>

          {mutation.isError ? <div className="error" data-testid="login-error">{extractErrorMessage(mutation.error)}</div> : null}

          <Button type="submit" size="lg" block disabled={mutation.isPending} data-testid="login-submit">
            Р’РѕР№С‚Рё
          </Button>
        </form>
      </FormShell>
    </div>
  )
}

