const express = require('express');
const csvService = require("../../src/services/csvService");
const apiRoute = require("../../src/controller/api");
const request = require("supertest")

jest.mock('../../src/services/csvService')
describe("api", ()=>{
    const app = express();
    app.use(apiRoute)
    beforeEach(()=>{
        csvService.getData.mockReturnValue({columns: {exposed: [2, 3], hour: [1, 2]}})
        csvService.getHeaders.mockReturnValue({headers: ["hour", "susceptible"]})
    })

    it("should get data", async ()=>{
        await request(app).get('/data')
            .expect(200)
            .expect({columns: {exposed: [2, 3], hour: [1, 2]}})
    })

    it("should get headers", async ()=>{
        await request(app).get('/headers')
            .expect(200)
            .expect({headers: ["hour", "susceptible"]})
    })
})