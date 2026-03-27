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
      showToast({ type: 'success', title: 'РђРєРєР°СѓРЅС‚ СЃРѕР·РґР°РЅ' })
      navigate('/')
    },
    onError: (error) => {
      showToast({ type: 'error', title: 'РћС€РёР±РєР° СЂРµРіРёСЃС‚СЂР°С†РёРё', description: extractErrorMessage(error) })
    },
  })

  return (
    <div className="page page--form" data-testid="register-page">
      <FormShell
        title="Р РµРіРёСЃС‚СЂР°С†РёСЏ"
        subtitle="РЎРѕР·РґР°Р№С‚Рµ Р°РєРєР°СѓРЅС‚, С‡С‚РѕР±С‹ РѕС„РѕСЂРјР»СЏС‚СЊ Р·Р°РєР°Р·С‹ РІ РїР°СЂСѓ РєР»РёРєРѕРІ."
        width="sm"
        variant="page"
        footer={
          <p className="muted">
            РЈР¶Рµ РµСЃС‚СЊ Р°РєРєР°СѓРЅС‚? <Link to="/login" data-testid="register-go-login">Р’РѕР№С‚Рё</Link>
          </p>
        }
      >
        <form onSubmit={handleSubmit((values) => mutation.mutate(values))} data-testid="register-form">
          <div className="form-row">
            <label htmlFor="fullName">РРјСЏ Рё С„Р°РјРёР»РёСЏ</label>
            <Input
              id="fullName"
              data-testid="register-full-name"
              hasError={Boolean(errors.fullName)}
              placeholder="РРІР°РЅ РџРµС‚СЂРѕРІ"
              {...register('fullName', {
                required: 'РЈРєР°Р¶РёС‚Рµ РёРјСЏ',
                minLength: { value: 2, message: 'РњРёРЅРёРјСѓРј 2 СЃРёРјРІРѕР»Р°' },
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
              data-testid="register-password"
              hasError={Boolean(errors.password)}
              placeholder="РњРёРЅРёРјСѓРј 8 СЃРёРјРІРѕР»РѕРІ"
              type="password"
              {...register('password', {
                required: 'Р’РІРµРґРёС‚Рµ РїР°СЂРѕР»СЊ',
                minLength: { value: 8, message: 'РњРёРЅРёРјСѓРј 8 СЃРёРјРІРѕР»РѕРІ' },
              })}
            />
            {errors.password ? <div className="error-text">{errors.password.message}</div> : null}
          </div>

          {mutation.isError ? <div className="error" data-testid="register-error">{extractErrorMessage(mutation.error)}</div> : null}

          <Button type="submit" size="lg" block disabled={mutation.isPending} data-testid="register-submit">
            РЎРѕР·РґР°С‚СЊ Р°РєРєР°СѓРЅС‚
          </Button>
        </form>
      </FormShell>
    </div>
  )
}

