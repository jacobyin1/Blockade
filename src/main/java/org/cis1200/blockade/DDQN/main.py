import time

from torch import optim

from data_gather import environment, experience_replay, simulator
import matplotlib.pyplot as plt
from model import qmodel


# f(0) = 1, f(15) = 0.05, f(30) = 0.001
def e_func(n):
    n_train_k = n / 1000
    a = 1.00263
    b = 0.82177
    c = -0.00263
    return a * (b ** (-1 * n_train_k)) + c


env = environment.Environment(20, (15, 15), (5, 5))
replay = experience_replay.ExperienceReplay(10000, env)
qmodel = qmodel.QModel(e_func, (418, 418, 418), device='cuda')
qmodel.load_model()

optimizer = optim.Adam(qmodel.parameters(), lr=0.0001)

for epoch in range(10):
    print(epoch)
    replay.gather(qmodel, episodes=100)

    if epoch % 10 == 0:
        sim_states = replay.get_episode(qmodel)
        fig, ax = plt.subplots(figsize=(10, 10))
        anim = simulator.simulate(fig, ax, sim_states)
        plt.show()

    for n_train in range(100):
        data = replay.sample(batch_size=25, device='cuda')
        qmodel.train(data, optimizer)
    qmodel.update_target()

fig, ax = plt.subplots(figsize=(10, 10))
qmodel.graph_loss(ax)

qmodel.save_model()





