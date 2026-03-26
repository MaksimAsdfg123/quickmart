import axios from 'axios'

export function extractErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const responseData = error.response?.data as { message?: string; fieldErrors?: Record<string, string> } | undefined

    if (responseData?.fieldErrors && Object.keys(responseData.fieldErrors).length > 0) {
      const details = Object.entries(responseData.fieldErrors)
        .map(([field, message]) => `${field}: ${message}`)
        .join('; ')
      return `${responseData.message ?? 'Ошибка валидации'} (${details})`
    }

    if (responseData?.message) {
      return responseData.message
    }

    if (error.response?.status === 401) {
      return 'Требуется авторизация'
    }
    if (error.response?.status === 403) {
      return 'Недостаточно прав для выполнения действия'
    }
    if (error.response?.status === 404) {
      return 'Запрошенный ресурс не найден'
    }

    return error.message ?? 'Ошибка запроса'
  }

  return error instanceof Error ? error.message : 'Неизвестная ошибка'
}
