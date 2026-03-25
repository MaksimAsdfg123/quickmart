import { ButtonLink } from '../shared/components/ui/ButtonLink'
import { FormShell } from '../shared/components/ui/FormShell'

export function NotFoundPage() {
  return (
    <div className="page page--form">
      <FormShell
        title="Страница не найдена"
        subtitle="Проверьте адрес страницы или вернитесь к каталогу."
        width="sm"
        variant="page"
        footer={<ButtonLink to="/">Вернуться в каталог</ButtonLink>}
      >
        <p className="muted">Если страница открылась по старой ссылке, начните путь заново из каталога.</p>
      </FormShell>
    </div>
  )
}
