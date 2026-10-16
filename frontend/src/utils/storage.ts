export const setItem = <T>(key: string, value: T): void => {
  try {
    localStorage.setItem(key, JSON.stringify(value))
  } catch (e) {
    console.error('Error saving to localStorage:', e)
  }
}

export const getItem = <T>(key: string): T | null => {
  try {
    const item = localStorage.getItem(key)
    return item ? JSON.parse(item) : null
  } catch (e) {
    console.error('Error reading from localStorage:', e)
    return null
  }
}

export const removeItem = (key: string): void => {
  try {
    localStorage.removeItem(key)
  } catch (e) {
    console.error('Error removing from localStorage:', e)
  }
}
