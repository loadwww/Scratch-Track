import Dexie from 'dexie'

class CaiJiDatabase extends Dexie {
  constructor() {
    super('CaiJiDB')
    this.version(1).stores({
      diaries: '++id, date, isHighlighted',
      lotteries: '++id, date, month',
      scratchRecords: '++id, date'
    })
  }
}

export const db = new CaiJiDatabase()
